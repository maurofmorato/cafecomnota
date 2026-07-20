package com.maurofmorato.cafecomnota.data.auth

/** Error shown when the Supabase access token can no longer be used. */
class AuthenticationExpiredException : IllegalStateException(
    "Sua sessão expirou. Entre novamente para continuar."
)

fun isAuthenticationExpired(
    statusCode: Int,
    responseBody: String
): Boolean {
    val normalized = responseBody.lowercase()

    return statusCode == 401 ||
        statusCode == 403 ||
        normalized.contains("jwt expired") ||
        normalized.contains("invalid jwt") ||
        normalized.contains("token has expired")
}
