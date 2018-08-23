package io.stanwood.debugapp.features.analytics

import java.util.*

data class AnalyticsData(val timestamp: Date, val event: String, val properties: List<Pair<String, String>>? = null, val stacktrace: String? = null)