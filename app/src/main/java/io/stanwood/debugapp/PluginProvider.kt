package io.stanwood.debugapp

import io.stanwood.debugapp.features.analytics.AnalyticsPlugin
import io.stanwood.debugapp.features.webrequest.WebRequestPlugin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginProvider @Inject constructor(private val analyticsPlugin: AnalyticsPlugin,
                                         private val webRequestPlugin: WebRequestPlugin){
    val plugins= mapOf(Pair(0,analyticsPlugin),Pair(1,webRequestPlugin))
}