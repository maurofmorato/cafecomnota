package com.maurofmorato.cafecomnota.data.auth

data class AuthSession(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)
