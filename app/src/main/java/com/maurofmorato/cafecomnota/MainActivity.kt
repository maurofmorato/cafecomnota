package com.maurofmorato.cafecomnota

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.maurofmorato.cafecomnota.analytics.CafeAnalytics

class MainActivity : ComponentActivity() {
    private val pendingDeepLink = mutableStateOf<String?>(null)
    private val updateReady = mutableStateOf(false)
    private lateinit var appUpdateManager: AppUpdateManager

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // A Play Store controla o resultado. Se o usuário cancelar, o app segue normalmente.
    }

    private val updateListener = InstallStateUpdatedListener { state ->
        updateReady.value = state.installStatus() == InstallStatus.DOWNLOADED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CafeAnalytics.init(applicationContext)
        captureDeepLink(intent)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(updateListener)
        checkForPlayStoreUpdate()

        setContent {
            CafeComNotaApp(
                authDeepLink = pendingDeepLink.value,
                onAuthDeepLinkConsumed = {
                    pendingDeepLink.value = null
                },
                updateReadyToInstall = updateReady.value,
                onInstallUpdate = {
                    updateReady.value = false
                    appUpdateManager.completeUpdate()
                },
                onDismissUpdate = {
                    updateReady.value = false
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                updateReady.value = info.installStatus() == InstallStatus.DOWNLOADED
            }
        }
    }

    override fun onDestroy() {
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(updateListener)
        }

        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureDeepLink(intent)
    }

    private fun captureDeepLink(intent: Intent?) {
        val uriString = intent?.data?.toString()

        if (!uriString.isNullOrBlank()) {
            pendingDeepLink.value = uriString
        }
    }

    private fun checkForPlayStoreUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val flexibleUpdateAvailable =
                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (flexibleUpdateAvailable) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }
}
