package io.stanwood.debugapp.features

import android.view.View

interface DebugPlugin {
    val pluginIcons: Array<Int>?
    fun create(): View
    fun destroy()
    fun onToolbarIconClicked(position: Int)
}