package io.stanwood.debugapp.features.analytics

import io.stanwood.debugapp.features.HasViewType


data class StacktraceViewModel(val trace: String) : HasViewType {
    companion object {
        const val VIEWTYPE = 1
    }

    override val viewType = VIEWTYPE
}