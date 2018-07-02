package io.stanwood.debugapp

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import javax.inject.Inject


class SettingsService @Inject constructor (private val context:Application,private val resources: Resources){

    fun saveViewSize(x: Int, y: Int, width: Int, height: Int) {
        context.getSharedPreferences("data", Context.MODE_PRIVATE)
                ?.apply {
                    edit().putInt("x", x).putInt("y", y).putInt("w", width).putInt("h", height).apply()
                }
    }

    fun getViewSize(): Rect {
        return context.getSharedPreferences("data", Context.MODE_PRIVATE)
                .let {
                    val left = it.getInt("x", 0)
                    val top = it.getInt("y", 0)
                    Rect(
                            left,
                            top,
                    left + it.getInt("w", resources.getDimension(R.dimen.overlay_expanded_min_size).toInt()),
                    top + it.getInt("h", resources.getDimension(R.dimen.overlay_expanded_min_size).toInt()))
                }
    }
}