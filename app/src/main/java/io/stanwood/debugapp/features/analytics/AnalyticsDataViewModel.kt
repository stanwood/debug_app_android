package io.stanwood.debugapp.features.analytics

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import io.stanwood.debugapp.BR


class AnalyticsDataViewModel(val analyticsData: AnalyticsData, val callback: (AnalyticsDataViewModel)->Unit) : BaseObservable() {

    val timestamp = analyticsData.timestamp.toString()
    val event =analyticsData.event

    var selected: Boolean = false
        @Bindable get
        set(value) {
            field    = value
            notifyPropertyChanged(BR.selected)
        }
}