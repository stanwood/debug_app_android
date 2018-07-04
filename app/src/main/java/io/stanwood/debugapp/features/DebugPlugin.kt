package io.stanwood.debugapp.features

import android.view.View

interface DebugPlugin {
    fun create(): View
    fun createToolbar(): View?
    fun destroy()
}