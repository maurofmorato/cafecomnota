package com.maurofmorato.cafecomnota.data.auth

import android.content.Context
import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SupabaseAuthRepository(
    context: Context
) {
    private val appContext = context.applicationContext

    private val preferences = appContext.getSharedPreferences(
        "cafecomnota_auth",
        Context.MODE_PRIVATE
    )

    suspend fun loginWithEmailPassword(
        email: String,
        password: String
    ): AuthSession {
        return withContext(Dispatchers.IO) {
            val url = URL("${SupabaseConfig.BASE_URL}/auth/v1/token?grant_type=password")

            val body = JSONObject()
                .put("email", email.trim())
                .put("password", password)
                .toString()

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 12_000
                doOutput = true

                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            try {
                connection.outputStream.use { output ->
                    output.write(body.toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseBody = if (statusCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                }

                if (statusCode !in 200..299) {
                    throw IllegalStateException(parseErrorMessage(responseBody))
                }

                val session = parseSession(responseBody)
                saveSession(session)
                session
            } finally {
                connection.disconnect()
            }
        }
    }

    fun getSavedSession(): AuthSession? {
        val userId = preferences.getString(KEY_USER_ID, null)
        val email = preferences.getString(KEY_EMAIL, null)
        val accessToken = preferences.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null)

        if (
            userId.isNullOrBlank() ||
            email.isNullOrBlank() ||
            accessToken.isNullOrBlank() ||
            refreshToken.isNullOrBlank()
        ) {
            return null
        }

        return AuthSession(
            userId = userId,
            email = email,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun logout() {
        preferences.edit()
            .clear()
            .apply()
    }

    private fun parseSession(
        responseBody: String
    ): AuthSession {
        val json = JSONObject(responseBody)
        val user = json.getJSONObject("user")

        val userId = user.getString("id")
        val email = user.optString("email", "")

        return AuthSession(
            userId = userId,
            email = email,
            accessToken = json.getString("access_token"),
            refreshToken = json.optString("refresh_token", "")
        )
    }

    private fun parseErrorMessage(
        responseBody: String
    ): String {
        if (responseBody.isBlank()) {
            return "Não foi possível fazer login. Tente novamente."
        }

        return try {
            val json = JSONObject(responseBody)

            json.optString("msg")
                .ifBlank {
                    json.optString("message")
                }
                .ifBlank {
                    json.optString("error_description")
                }
                .ifBlank {
                    "Não foi possível fazer login. Verifique email e senha."
                }
        } catch (_: Exception) {
            "Não foi possível fazer login. Verifique email e senha."
        }
    }

    private fun saveSession(
        session: AuthSession
    ) {
        preferences.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .apply()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
