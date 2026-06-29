package com.maurofmorato.cafecomnota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CafeAnalytics.init(applicationContext)

        setContent {
            CafeComNotaApp()
        }
    }
}
