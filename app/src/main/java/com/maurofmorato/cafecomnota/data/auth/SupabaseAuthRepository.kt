package com.maurofmorato.cafecomnota.data.auth

import android.content.Context
import android.net.Uri
import com.maurofmorato.cafecomnota.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                val responseBody = readResponse(connection, statusCode)

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

    suspend fun loginFromDeepLink(
        uriString: String
    ): AuthSession {
        return withContext(Dispatchers.IO) {
            val params = parseUriParameters(uriString)

            val error = params["error_description"]
                ?: params["error"]
                ?: params["error_code"]

            if (!error.isNullOrBlank()) {
                throw IllegalStateException(error.replace("+", " "))
            }

            val code = params["code"]
            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"].orEmpty()

            if (accessToken.isNullOrBlank()) {
                if (!code.isNullOrBlank()) {
                    throw IllegalStateException(
                        "O retorno do Google veio em modo código. Para essa etapa, use o fluxo padrão do Supabase com deep link e confirme as Redirect URLs."
                    )
                }

                throw IllegalStateException("Não encontrei token de autenticação no retorno do login.")
            }

            val session = loadSessionFromAccessToken(
                accessToken = accessToken,
                refreshToken = refreshToken
            )

            saveSession(session)
            session
        }
    }

    fun buildGoogleLoginUrl(): String {
        val redirectTo = encode(DEEP_LINK_CALLBACK)

        return "${SupabaseConfig.BASE_URL}/auth/v1/authorize" +
            "?provider=google" +
            "&redirect_to=$redirectTo" +
            "&scopes=${encode("email profile")}" 
    }

    suspend fun requestPasswordReset(
        email: String
    ) {
        withContext(Dispatchers.IO) {
            val redirectTo = encode(DEEP_LINK_RESET_PASSWORD)
            val url = URL("${SupabaseConfig.BASE_URL}/auth/v1/recover?redirect_to=$redirectTo")

            val body = JSONObject()
                .put("email", email.trim())
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
                val responseBody = readResponse(connection, statusCode)

                if (statusCode !in 200..299) {
                    throw IllegalStateException(parseErrorMessage(responseBody, "Não foi possível enviar recuperação de senha."))
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun updatePassword(
        accessToken: String,
        newPassword: String
    ) {
        withContext(Dispatchers.IO) {
            val url = URL("${SupabaseConfig.BASE_URL}/auth/v1/user")

            val body = JSONObject()
                .put("password", newPassword)
                .toString()

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 12_000
                readTimeout = 12_000
                doOutput = true

                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            try {
                connection.outputStream.use { output ->
                    output.write(body.toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseBody = readResponse(connection, statusCode)

                if (statusCode !in 200..299) {
                    throw IllegalStateException(parseErrorMessage(responseBody, "Não foi possível alterar a senha."))
                }
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

    private fun loadSessionFromAccessToken(
        accessToken: String,
        refreshToken: String
    ): AuthSession {
        val url = URL("${SupabaseConfig.BASE_URL}/auth/v1/user")

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000

            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val statusCode = connection.responseCode
            val responseBody = readResponse(connection, statusCode)

            if (statusCode !in 200..299) {
                throw IllegalStateException(parseErrorMessage(responseBody, "Não foi possível buscar usuário autenticado."))
            }

            val json = JSONObject(responseBody)

            return AuthSession(
                userId = json.getString("id"),
                email = json.optString("email", ""),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } finally {
            connection.disconnect()
        }
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
        responseBody: String,
        fallback: String = "Não foi possível fazer login. Verifique email e senha."
    ): String {
        if (responseBody.isBlank()) {
            return fallback
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
                    json.optString("hint")
                }
                .ifBlank {
                    fallback
                }
        } catch (_: Exception) {
            fallback
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

    private fun readResponse(
        connection: HttpURLConnection,
        statusCode: Int
    ): String {
        return if (statusCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
    }

    private fun parseUriParameters(
        uriString: String
    ): Map<String, String> {
        val uri = Uri.parse(uriString)
        val params = linkedMapOf<String, String>()

        uri.queryParameterNames.forEach { name ->
            params[name] = uri.getQueryParameter(name).orEmpty()
        }

        parseParameterString(uri.encodedFragment.orEmpty()).forEach { (key, value) ->
            params[key] = value
        }

        parseParameterString(uri.encodedQuery.orEmpty()).forEach { (key, value) ->
            params[key] = value
        }

        return params
    }

    private fun parseParameterString(
        value: String
    ): Map<String, String> {
        if (value.isBlank()) {
            return emptyMap()
        }

        return value
            .split("&")
            .mapNotNull { pair ->
                val index = pair.indexOf("=")

                if (index <= 0) {
                    null
                } else {
                    val key = decode(pair.substring(0, index))
                    val parameterValue = decode(pair.substring(index + 1))

                    key to parameterValue
                }
            }
            .toMap()
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8.toString()
        )
    }

    private fun decode(value: String): String {
        return URLDecoder.decode(
            value,
            StandardCharsets.UTF_8.toString()
        )
    }

    companion object {
        const val DEEP_LINK_CALLBACK = "cafecomnota://auth/callback"
        const val DEEP_LINK_RESET_PASSWORD = "cafecomnota://auth/reset-password"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
