package io.stanwood.debugapp.features

import io.stanwood.debugapp.R
import io.stanwood.debugapp.features.analytics.AnalyticsPlugin
import io.stanwood.debugapp.features.webrequest.WebRequestPlugin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginProvider @Inject constructor(private val analyticsPlugin: AnalyticsPlugin,
                                         private val webRequestPlugin: WebRequestPlugin) {
    val plugins = mapOf(
            Pair(0, PluginConfig(analyticsPlugin, "Analytics", R.drawable.ic_timeline_black_24dp, 0)),
            Pair(1, PluginConfig(webRequestPlugin, "Requests", R.drawable.ic_cloud_black_24dp, 1)))
}

data class PluginConfig(val plugin: DebugPlugin, val name: String, val iconResId: Int, val id: Int)