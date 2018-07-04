package io.stanwood.debugapp.features.overlay

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.view.View
import com.android.databinding.library.baseAdapters.BR

data class DrawerItem(val title: String, val iconResId: Int, val id: Int, val itemClickListener: (DrawerItem) -> Unit) : BaseObservable() {
    fun onClicked(view: View?) {
        itemClickListener(this)
    }

    var selected: Boolean = false
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.selected)
        }
}

