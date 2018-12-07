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

package io.stanwood.debugapp.services

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import io.stanwood.debugapp.R
import javax.inject.Inject


class SettingsRepository @Inject constructor(private val context: Application, private val resources: Resources) {

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