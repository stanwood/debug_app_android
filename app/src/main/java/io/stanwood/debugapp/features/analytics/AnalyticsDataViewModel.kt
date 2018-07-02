package io.stanwood.debugapp.features.analytics

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.stanwood.debugapp.BR
import java.text.DateFormat
import java.util.*


class AnalyticsDataViewModel(val analyticsData: AnalyticsData, val callback: (AnalyticsDataViewModel) -> Unit) : BaseObservable() {
    val event = analyticsData.event
    var selected: Boolean = false
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.selected)
        }

    val timestamp by lazy {
        DateFormat.getTimeInstance().format(Date(analyticsData.timestamp)) ?: ""
    }

}