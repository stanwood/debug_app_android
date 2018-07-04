package io.stanwood.debugapp.features.analytics


data class StacktraceViewModel(val trace: String) : HasViewType {
    override val viewType = 1
}