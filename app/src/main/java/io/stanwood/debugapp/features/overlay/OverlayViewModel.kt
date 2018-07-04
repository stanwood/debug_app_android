package io.stanwood.debugapp.features.overlay

import android.databinding.BaseObservable
import android.databinding.Bindable
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.features.PluginProvider
import javax.inject.Inject

class OverlayViewModel @Inject constructor(val pluginProvider: PluginProvider) : BaseObservable() {
    private val drawerItemClickListener: (DrawerItem) -> Unit = {
        selectedItem = it
    }
    val items = pluginProvider.plugins.mapNotNull {
        DrawerItem(it.value.name, it.value.iconResId, it.value.id, drawerItemClickListener)
    }

    @Bindable
    var selectedItem: DrawerItem? = null
        set(value) {
            if (field != value) {
                field?.selected = false
                field = value
                field?.selected = true
                notifyPropertyChanged(BR.selectedItem)
            }
        }
}