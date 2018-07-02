package io.stanwood.debugapp.features.overlay

import android.app.Application
import android.databinding.Observable
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.PluginProvider
import io.stanwood.debugapp.SettingsService
import io.stanwood.debugapp.databinding.ItemDrawerBinding
import io.stanwood.debugapp.databinding.ViewOverlayBinding
import io.stanwood.debugapp.features.DebugPlugin
import io.stanwood.framework.databinding.recyclerview.DataBindingViewHolder
import io.stanwood.framework.databinding.recyclerview.ObservableListBindingAdapter
import org.json.JSONObject
import javax.inject.Inject

class Overlay @Inject constructor(private val context: Application,
                                  private val settingsService: SettingsService,
                                  private val viewModel: OverlayViewModel,
                                  private val pluginProvider: PluginProvider) {
    val contentView: View
        get() = binding.root

    private val iconClickListener: ((Int) -> Unit) = {
        activePlugin?.onToolbarIconClicked(it)
    }

    private val binding by lazy {
        ViewOverlayBinding.inflate(LayoutInflater.from(context))
                .apply {
                    viewModel.addOnPropertyChangedCallback(propertyChangedCallback)
                    overlay.iconClickListener = iconClickListener
                    overlay.viewChangedCallback = { x, y, w, h -> settingsService.saveViewSize(x, y, w, h) }
                    rcvDrawer.layoutManager = LinearLayoutManager(context)
                    rcvDrawer.adapter = Adapter(LayoutInflater.from(context))
                    vm = viewModel
                    executePendingBindings()
                }
    }


    private val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (propertyId == BR.selectedItem) {
                activePlugin = pluginProvider.plugins[viewModel.selectedItem?.id]
            }
        }
    }

    private var activePlugin: DebugPlugin? = null
        set(value) {
            if (field != value) {
                field = value
                binding.container.removeAllViews()
                value?.apply {
                    binding.container.addView(create())
                    binding.overlay.setToolbarIcons(pluginIcons)
                }
            }
        }


    private fun JSONObject.getStringOrDefault(name: String, default: String = "") = if (has(name)) getString(name) else default


    private class Adapter(inflater: LayoutInflater) : ObservableListBindingAdapter<DrawerItem>(inflater) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                DataBindingViewHolder(ItemDrawerBinding.inflate(inflater, viewGroup, false))

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: DrawerItem, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }
}

