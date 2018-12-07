/*
 * Copyright (c) 2018 stanwood GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.stanwood.debugapp.features

import io.stanwood.debugapp.R
import io.stanwood.debugapp.features.analytics.AnalyticsPlugin
import io.stanwood.debugapp.features.webrequest.WebRequestPlugin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginProvider @Inject constructor(private val analyticsPlugin: AnalyticsPlugin,
                                         private val webRequestPlugin: WebRequestPlugin) {
    val plugins = sortedMapOf(
            Pair(0, PluginConfig(analyticsPlugin, "Analytics", R.drawable.ic_timeline_black_24dp, 0)),
            Pair(1, PluginConfig(webRequestPlugin, "Requests", R.drawable.ic_cloud_black_24dp, 1)))
}

data class PluginConfig(val plugin: DebugPlugin, val name: String, val iconResId: Int, val id: Int)