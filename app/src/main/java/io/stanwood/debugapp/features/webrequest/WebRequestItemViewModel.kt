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