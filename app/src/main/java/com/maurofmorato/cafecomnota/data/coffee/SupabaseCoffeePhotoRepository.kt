package com.maurofmorato.cafecomnota.data.coffee

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.auth.isAuthenticationExpired
import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Envia rótulos de forma idempotente: cada posição tem um caminho estável.
 * Ao repetir o envio, fotos já registradas são preservadas e somente as faltantes seguem.
 */
class SupabaseCoffeePhotoRepository {
    suspend fun uploadPendingPhotos(
        context: Context,
        coffeeId: String,
        userId: String,
        accessToken: String,
        photos: List<CoffeePhotoUpload>,
        onProgress: (uploaded: Int, total: Int) -> Unit
    ) {
        if (photos.isEmpty()) return

        withContext(Dispatchers.IO) {
            val limitedPhotos = photos.take(CoffeePhotoRules.MAX_PHOTOS)
            val alreadyUploaded = existingPhotoOrders(coffeeId, accessToken).toMutableSet()
            markUploadStatus(coffeeId, accessToken, "enviando", alreadyUploaded.size)
            onProgress(alreadyUploaded.size, limitedPhotos.size)

            try {
                limitedPhotos.forEachIndexed { index, photo ->
                    if (index in alreadyUploaded) return@forEachIndexed

                    val storagePath = "$userId/$coffeeId/${index}_${photo.label}.jpg"
                    uploadObject(storagePath, accessToken, compressedJpeg(context, photo.uri))
                    upsertPhotoRecord(coffeeId, storagePath, photo.label, index, userId, accessToken)
                    alreadyUploaded += index
                    markUploadStatus(coffeeId, accessToken, "enviando", alreadyUploaded.size)
                    onProgress(alreadyUploaded.size, limitedPhotos.size)
                }
                markUploadStatus(coffeeId, accessToken, "concluida", alreadyUploaded.size)
            } catch (error: Throwable) {
                runCatching { markUploadStatus(coffeeId, accessToken, "falhou", alreadyUploaded.size) }
                throw IllegalStateException(
                    "O café foi salvo. ${alreadyUploaded.size}/${limitedPhotos.size} fotos foram enviadas. " +
                        "Confira a conexão e toque em Salvar café para continuar.",
                    error
                )
            }
        }
    }

    private fun existingPhotoOrders(coffeeId: String, accessToken: String): Set<Int> {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafe_fotos?select=ordem" +
            "&cafe_id=eq.${encode(coffeeId)}"
        val response = executeRequest(endpoint, accessToken, "GET", null, null, null)
        return JSONArray(response).let { array ->
            buildSet {
                for (index in 0 until array.length()) add(array.getJSONObject(index).optInt("ordem"))
            }
        }
    }

    private fun markUploadStatus(coffeeId: String, accessToken: String, status: String, sent: Int) {
        val endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafes?id=eq.${encode(coffeeId)}"
        val body = JSONObject().put("fotos_status", status).put("fotos_enviadas", sent)
        executeRequest(endpoint, accessToken, "PATCH", body.toString().toByteArray(), "application/json", "return=minimal")
    }

    private fun compressedJpeg(context: Context, uri: android.net.Uri): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > CoffeePhotoRules.MAX_IMAGE_EDGE ||
            bounds.outHeight / sample > CoffeePhotoRules.MAX_IMAGE_EDGE) sample *= 2
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalStateException("Não foi possível preparar uma das fotos do rótulo.")
        return ByteArrayOutputStream().use { output ->
            val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, CoffeePhotoRules.JPEG_QUALITY, output)
            bitmap.recycle()
            if (!ok) throw IllegalStateException("Não foi possível compactar uma das fotos do rótulo.")
            output.toByteArray()
        }
    }

    private fun uploadObject(storagePath: String, accessToken: String, bytes: ByteArray) {
        executeRequest(
            "${SupabaseConfig.BASE_URL}/storage/v1/object/$BUCKET/$storagePath",
            accessToken,
            "POST",
            bytes,
            "image/jpeg",
            null,
            upsert = true
        )
    }

    private fun upsertPhotoRecord(
        coffeeId: String, storagePath: String, label: String, order: Int, userId: String, accessToken: String
    ) {
        val body = JSONObject()
            .put("cafe_id", coffeeId).put("storage_path", storagePath).put("rotulo", label)
            .put("ordem", order).put("enviada_por", userId)
        executeRequest(
            "${SupabaseConfig.BASE_URL}/rest/v1/cafe_fotos?on_conflict=cafe_id,ordem",
            accessToken, "POST", body.toString().toByteArray(), "application/json",
            "resolution=merge-duplicates,return=minimal"
        )
    }

    private fun executeRequest(
        endpoint: String, accessToken: String, method: String, body: ByteArray?, contentType: String?, prefer: String?, upsert: Boolean = false
    ): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = body != null
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            contentType?.let { setRequestProperty("Content-Type", it) }
            prefer?.let { setRequestProperty("Prefer", it) }
            if (upsert) setRequestProperty("x-upsert", "true")
        }
        try {
            body?.let { connection.outputStream.use { output -> output.write(it) } }
            val code = connection.responseCode
            val response = if (code in 200..299) {
                connection.inputStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            } else connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                if (isAuthenticationExpired(code, response)) throw AuthenticationExpiredException()
                throw IllegalStateException("Não foi possível enviar uma foto agora.")
            }
            return response
        } finally { connection.disconnect() }
    }

    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    private companion object { const val BUCKET = "cafe-rotulos" }
}
