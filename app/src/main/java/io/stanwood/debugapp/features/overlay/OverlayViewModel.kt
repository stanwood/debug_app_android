package io.stanwood.debugapp.features.overlay

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.R
import javax.inject.Inject

class OverlayViewModel @Inject constructor() : BaseObservable() {
    private val drawerItemClickListener: (DrawerItem) -> Unit = {
        selectedItem = it
    }

    val items = listOf(DrawerItem("Analytics", R.drawable.ic_clear_all_black_24dp, 0, drawerItemClickListener),
            DrawerItem("Requests", R.drawable.ic_crop_free_black_24dp, 1, drawerItemClickListener))

    @Bindable
    var selectedItem: DrawerItem? = items[0]
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedItem)
        }
}