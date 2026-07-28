package com.maurofmorato.cafecomnota.data.supabase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Baixa fotos de embalagem de um bucket privado respeitando as políticas RLS.
 * Erros de uma imagem retornam null para que a interface use o pacote neutro.
 */
object SupabaseCoffeeImageLoader {
    private const val BUCKET = "cafe-rotulos"
    private val cache = LruCache<String, Bitmap>(24)

    suspend fun load(storagePath: String): Bitmap? = withContext(Dispatchers.IO) {
        cache.get(storagePath) ?: download(storagePath)?.also { bitmap ->
            cache.put(storagePath, bitmap)
        }
    }

    private fun download(storagePath: String): Bitmap? {
        val encodedPath = storagePath
            .split("/")
            .joinToString("/") { segment ->
                URLEncoder.encode(segment, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
            }
        val endpoint = "${SupabaseConfig.BASE_URL}/storage/v1/object/authenticated/$BUCKET/$encodedPath"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
        }

        return try {
            if (connection.responseCode !in 200..299) {
                null
            } else {
                connection.inputStream.use(BitmapFactory::decodeStream)
            }
        } finally {
            connection.disconnect()
        }
    }
}
