package com.maurofmorato.cafecomnota

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics

class MainActivity : ComponentActivity() {
    private val pendingAuthDeepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CafeAnalytics.init(applicationContext)
        captureAuthDeepLink(intent)

        setContent {
            CafeComNotaApp(
                authDeepLink = pendingAuthDeepLink.value,
                onAuthDeepLinkConsumed = {
                    pendingAuthDeepLink.value = null
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureAuthDeepLink(intent)
    }

    private fun captureAuthDeepLink(intent: Intent?) {
        val uriString = intent?.data?.toString()

        if (!uriString.isNullOrBlank()) {
            pendingAuthDeepLink.value = uriString
        }
    }
}
