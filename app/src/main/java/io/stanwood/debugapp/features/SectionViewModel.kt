package io.stanwood.debugapp.features


data class SectionViewModel(val title: String) : HasViewType {
    companion object {
        const val VIEWTYPE = 2
    }

    override val viewType = VIEWTYPE
}