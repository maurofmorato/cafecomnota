package com.maurofmorato.cafecomnota.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrown
import com.maurofmorato.cafecomnota.ui.theme.CoffeeBrownDark
import com.maurofmorato.cafecomnota.ui.theme.CoffeeCard
import com.maurofmorato.cafecomnota.ui.theme.CoffeeMuted

@Composable
fun CoffeeShareDialog(
    coffeeId: String,
    coffeeName: String,
    onDismiss: () -> Unit
) {
    val link = remember(coffeeId) { coffeeShareLink(coffeeId) }

    ShareQrDialog(
        title = "Compartilhar café",
        subtitle = coffeeName,
        link = link,
        qrDescription = "QR Code para compartilhar $coffeeName",
        helpText = "Quem tiver o Café com nota instalado poderá abrir este café pelo QR Code ou pelo link.",
        shareText = "Veja o café $coffeeName no Café com nota: $link",
        chooserTitle = "Compartilhar café",
        onDismiss = onDismiss
    )
}

@Composable
fun AppShareDialog(
    onDismiss: () -> Unit
) {
    val link = TESTING_APP_LINK

    ShareQrDialog(
        title = "Compartilhar o app",
        subtitle = "Café com nota — versão de teste",
        link = link,
        qrDescription = "QR Code para participar do teste do Café com nota",
        helpText = "A pessoa precisa usar a conta Google cadastrada na lista de testadores para instalar esta versão.",
        shareText = "Participe do teste do Café com nota: $link",
        chooserTitle = "Compartilhar Café com nota",
        onDismiss = onDismiss
    )
}

@Composable
private fun ShareQrDialog(
    title: String,
    subtitle: String,
    link: String,
    qrDescription: String,
    helpText: String,
    shareText: String,
    chooserTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val qrCode = remember(link) { generateQrCode(link) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CoffeeCard
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = CoffeeBrownDark,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    color = CoffeeMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Image(
                    bitmap = qrCode.asImageBitmap(),
                    contentDescription = qrDescription,
                    modifier = Modifier.size(230.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = helpText,
                    color = CoffeeMuted,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { clipboardManager.setText(AnnotatedString(link)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text(" Copiar")
                    }

                    Button(
                        onClick = {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    },
                                    chooserTitle
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.Share, contentDescription = null)
                        Text(" Compartilhar")
                    }
                }
            }
        }
    }
}

private fun coffeeShareLink(coffeeId: String): String = "cafecomnota://coffee/$coffeeId"

private const val TESTING_APP_LINK =
    "https://play.google.com/apps/test/com.maurofmorato.cafecomnota/14"

private fun generateQrCode(content: String): Bitmap {
    val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 720, 720)
    return matrix.toBitmap()
}

private fun BitMatrix.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            pixels[y * width + x] = if (get(x, y)) 0xFF301707.toInt() else 0xFFFFFFFF.toInt()
        }
    }

    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
