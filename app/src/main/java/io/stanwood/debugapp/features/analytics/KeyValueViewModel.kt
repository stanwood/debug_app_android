package io.stanwood.debugapp.features.analytics


data class KeyValueViewModel(val key: String, val value: String) : HasViewType {
    override val viewType = 0
}