package io.stanwood.debugapp.features


data class KeyValueViewModel(val key: String, val value: String) : HasViewType {
    companion object {
        const val VIEWTYPE = 0
    }

    override val viewType = VIEWTYPE
}