package io.stanwood.debugapp.features.overlay

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.PluginProvider
import javax.inject.Inject

class OverlayViewModel @Inject constructor(val pluginProvider: PluginProvider) : BaseObservable() {
    private val drawerItemClickListener: (DrawerItem) -> Unit = {
        selectedItem = it
    }
    val items = pluginProvider.plugins.mapNotNull {
        DrawerItem(it.value.name, it.value.iconResId, it.value.id, drawerItemClickListener)
    }
    @Bindable
    var selectedItem: DrawerItem? = items[0]
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedItem)
        }
}