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

package io.stanwood.debugapp.features.webrequest

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.net.Uri
import io.stanwood.debugapp.BR

class WebRequestItemViewModel(var webRequestData: WebRequestData, val callback: (WebRequestItemViewModel) -> Unit) : BaseObservable() {
    @Bindable
    var responseCode = ""
    var requestMethod = ""
    var url = ""

    init {
        url = Uri.parse(webRequestData.url)?.let {
            "${it.host}${it.path}${if (!it.query.isNullOrEmpty()) "?" + it.query else ""}"
        } ?: webRequestData.url
        requestMethod = webRequestData.method ?: ""
        update(webRequestData)
    }

    fun update(webRequestData: WebRequestData) {
        responseCode = if (webRequestData.responseCode < 0) "" else webRequestData.responseCode.toString()
        if (webRequestData.duration >= 0) {
            responseCode += "\r\n${webRequestData.duration} ms"
        }
        this.webRequestData = webRequestData
        notifyPropertyChanged(BR.responseCode)
    }

    var selected: Boolean = false
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.selected)
        }
}