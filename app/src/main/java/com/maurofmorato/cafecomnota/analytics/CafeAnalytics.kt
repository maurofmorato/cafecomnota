package com.maurofmorato.cafecomnota.analytics

import android.util.Log

object CafeAnalytics {
    private const val TAG = "CafeAnalytics"

    fun logScreen(screenName: String) {
        Log.d(TAG, "screen=$screenName")
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

    const val START_REVIEW = "start_review"
    const val CALCULATE_PRICE_KG = "calculate_price_kg"
    const val SAVE_REVIEW_TAP = "save_review_tap"

    const val START_ADD_COFFEE = "start_add_coffee"
    const val SAVE_NEW_COFFEE_TAP = "save_new_coffee_tap"
}
