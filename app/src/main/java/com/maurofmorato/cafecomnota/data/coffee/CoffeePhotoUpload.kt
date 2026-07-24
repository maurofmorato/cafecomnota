package com.maurofmorato.cafecomnota.data.coffee

import android.net.Uri

/**
 * Foto ainda local, escolhida pelo usuário durante o cadastro.
 * A primeira foto é usada como capa quando o café for aprovado.
 */
data class CoffeePhotoUpload(
    val uri: Uri,
    val label: String
)
