package io.stanwood.debugapp.features.webrequest

import android.app.Application
import android.view.View
import io.stanwood.debugapp.features.DebugPlugin
import javax.inject.Inject

class WebRequestPlugin @Inject constructor(val context: Application) : DebugPlugin {
    override fun destroy() {

    }

    override fun create(): View {
        return View(context)
    }

    override fun onToolbarIconClicked(position: Int) {

    }

    override val pluginIcons: Array<Int>? = null

}