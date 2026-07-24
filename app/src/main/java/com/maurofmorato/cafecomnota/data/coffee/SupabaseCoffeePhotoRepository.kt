package com.maurofmorato.cafecomnota.data.coffee

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.maurofmorato.cafecomnota.data.auth.AuthenticationExpiredException
import com.maurofmorato.cafecomnota.data.auth.isAuthenticationExpired
import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Envia as fotos do rótulo para um bucket privado do Supabase. */
class SupabaseCoffeePhotoRepository {
    suspend fun uploadPhotos(
        context: Context,
        coffeeId: String,
        userId: String,
        accessToken: String,
        photos: List<CoffeePhotoUpload>
    ) {
        if (photos.isEmpty()) return

        withContext(Dispatchers.IO) {
            photos.take(MAX_PHOTOS).forEachIndexed { index, photo ->
                val photoBytes = compressedJpeg(context, photo.uri)
                val fileName = "${index + 1}_${photo.label}_${System.currentTimeMillis()}.jpg"
                val storagePath = "$userId/$coffeeId/$fileName"

                uploadObject(
                    storagePath = storagePath,
                    accessToken = accessToken,
                    bytes = photoBytes
                )

                createPhotoRecord(
                    coffeeId = coffeeId,
                    storagePath = storagePath,
                    label = photo.label,
                    order = index,
                    userId = userId,
                    accessToken = accessToken
                )
            }
        }
    }

    private fun compressedJpeg(context: Context, uri: android.net.Uri): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_IMAGE_EDGE || bounds.outHeight / sampleSize > MAX_IMAGE_EDGE) {
            sampleSize *= 2
        }

        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            })
        } ?: throw IllegalStateException("Não foi possível preparar uma das fotos do rótulo.")

        return ByteArrayOutputStream().use { output ->
            val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            bitmap.recycle()
            if (!compressed) {
                throw IllegalStateException("Não foi possível compactar uma das fotos do rótulo.")
            }
            output.toByteArray()
        }
    }

    private fun uploadObject(
        storagePath: String,
        accessToken: String,
        bytes: ByteArray
    ) {
        val endpoint = "${SupabaseConfig.BASE_URL}/storage/v1/object/$BUCKET/$storagePath"
        executeRequest(
            endpoint = endpoint,
            accessToken = accessToken,
            method = "POST",
            body = bytes,
            contentType = "image/jpeg"
        )
    }

    private fun createPhotoRecord(
        coffeeId: String,
        storagePath: String,
        label: String,
        order: Int,
        userId: String,
        accessToken: String
    ) {
        val body = JSONObject()
            .put("cafe_id", coffeeId)
            .put("storage_path", storagePath)
            .put("rotulo", label)
            .put("ordem", order)
            .put("enviada_por", userId)

        executeRequest(
            endpoint = "${SupabaseConfig.BASE_URL}/rest/v1/cafe_fotos",
            accessToken = accessToken,
            method = "POST",
            body = body.toString().toByteArray(Charsets.UTF_8),
            contentType = "application/json"
        )
    }

    private fun executeRequest(
        endpoint: String,
        accessToken: String,
        method: String,
        body: ByteArray,
        contentType: String
    ) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Content-Type", contentType)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("x-upsert", "false")
        }

        try {
            connection.outputStream.use { it.write(body) }
            val statusCode = connection.responseCode
            val responseBody = if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (statusCode !in 200..299) {
                if (isAuthenticationExpired(statusCode, responseBody)) {
                    throw AuthenticationExpiredException()
                }
                throw IllegalStateException(
                    "O café foi salvo, mas uma foto não pôde ser enviada. " +
                        "Confira sua conexão e tente novamente."
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val BUCKET = "cafe-rotulos"
        const val MAX_PHOTOS = 5
        const val MAX_IMAGE_EDGE = 1600
        const val JPEG_QUALITY = 84
    }
}
