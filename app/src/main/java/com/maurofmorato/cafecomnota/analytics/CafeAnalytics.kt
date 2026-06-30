package com.maurofmorato.cafecomnota.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CafeAnalytics {
    private const val TAG = "CafeAnalytics"

    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var firebaseCrashlytics: FirebaseCrashlytics? = null

    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }

        if (firebaseCrashlytics == null) {
            firebaseCrashlytics = FirebaseCrashlytics.getInstance()
            firebaseCrashlytics?.setCrashlyticsCollectionEnabled(true)
            firebaseCrashlytics?.setCustomKey("app_name", "Cafe com nota")
        }

        Log.d(TAG, "Firebase Analytics/Crashlytics inicializados")
    }

    fun logScreen(screenName: String) {
        Log.d(TAG, "screen=$screenName")

        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }

        firebaseAnalytics?.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            params
        )

        firebaseCrashlytics?.log("screen=$screenName")
    }

    fun logEvent(
        eventName: String,
        params: Map<String, Any?> = emptyMap()
    ) {
        val formattedParams = params.entries.joinToString(
            separator = ", "
        ) { entry ->
            "${entry.key}=${entry.value}"
        }

        Log.d(TAG, "event=$eventName params={$formattedParams}")

        val bundle = Bundle()

        params.forEach { entry ->
            val key = sanitizeParamName(entry.key)

            when (val value = entry.value) {
                null -> bundle.putString(key, "null")
                is String -> bundle.putString(key, value.take(100))
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Float -> bundle.putDouble(key, value.toDouble())
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putString(key, value.toString())
                else -> bundle.putString(key, value.toString().take(100))
            }
        }

        firebaseAnalytics?.logEvent(
            sanitizeEventName(eventName),
            bundle
        )

        firebaseCrashlytics?.log("event=$eventName params={$formattedParams}")
    }

    fun setUserId(userId: String?) {
        firebaseAnalytics?.setUserId(userId)
        firebaseCrashlytics?.setUserId(userId.orEmpty())

        Log.d(TAG, "setUserId=${userId ?: "null"}")
    }

    fun setCoffeeContext(
        coffeeId: String,
        coffeeName: String
    ) {
        firebaseCrashlytics?.setCustomKey("coffee_id", coffeeId)
        firebaseCrashlytics?.setCustomKey("coffee_name", coffeeName.take(64))

        logEvent(
            eventName = "set_coffee_context",
            params = mapOf(
                "coffee_id" to coffeeId,
                "coffee_name" to coffeeName
            )
        )
    }

    fun recordNonFatal(
        throwable: Throwable,
        params: Map<String, Any?> = emptyMap()
    ) {
        val formattedParams = params.entries.joinToString(
            separator = ", "
        ) { entry ->
            "${entry.key}=${entry.value}"
        }

        Log.w(TAG, "non_fatal=${throwable.message} params={$formattedParams}", throwable)

        firebaseCrashlytics?.log("non_fatal params={$formattedParams}")
        params.forEach { entry ->
            val key = sanitizeParamName(entry.key)
            val value = entry.value?.toString() ?: "null"
            firebaseCrashlytics?.setCustomKey(key, value.take(100))
        }

        firebaseCrashlytics?.recordException(throwable)
    }

    private fun sanitizeEventName(value: String): String {
        val sanitized = value
            .trim()
            .replace(Regex("[^A-Za-z0-9_]"), "_")
            .take(40)

        return if (sanitized.firstOrNull()?.isLetter() == true) {
            sanitized
        } else {
            "event_$sanitized"
        }
    }

    private fun sanitizeParamName(value: String): String {
        val sanitized = value
            .trim()
            .replace(Regex("[^A-Za-z0-9_]"), "_")
            .take(40)

        return if (sanitized.isBlank()) {
            "param"
        } else {
            sanitized
        }
    }
}

object AnalyticsEvents {
    const val VIEW_HOME = "view_home"
    const val VIEW_SEARCH = "view_search"
    const val VIEW_RANKING = "view_ranking"
    const val VIEW_PROFILE = "view_profile"
    const val VIEW_COFFEE_DETAIL = "view_coffee_detail"

    const val NAVIGATE = "navigate"
    const val SEARCH_COFFEE = "search_coffee"
    const val SEARCH_NOT_FOUND = "search_not_found"
    const val CHANGE_RANKING_FILTER = "change_ranking_filter"
    const val CHANGE_LANGUAGE = "change_language"

    const val LOAD_COFFEES = "load_coffees"
    const val LOAD_COFFEES_FALLBACK = "load_coffees_fallback"

    const val START_REVIEW = "start_review"
    const val CALCULATE_PRICE_KG = "calculate_price_kg"
    const val SAVE_REVIEW_TAP = "save_review_tap"

    const val START_ADD_COFFEE = "start_add_coffee"
    const val SAVE_NEW_COFFEE_TAP = "save_new_coffee_tap"

    const val CRASHLYTICS_NON_FATAL_TEST = "crashlytics_non_fatal_test"
}
