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
import javax.inject.Inject

class Overlay @Inject constructor(private val context: Application,
                                  private val settingsService: SettingsService,
                                  private val viewModel: OverlayViewModel,
                                  private val pluginProvider: PluginProvider) {


    private val iconClickListener: ((Int) -> Unit) = {
        activePlugin?.onToolbarIconClicked(it)
    }

    private var binding: ViewOverlayBinding? = null

    val rootView = binding?.root

    fun create(): View =
            ViewOverlayBinding.inflate(LayoutInflater.from(context))
                    .let {
                        binding = it
                        viewModel.addOnPropertyChangedCallback(propertyChangedCallback)
                        it.overlay.iconClickListener = iconClickListener
                        it.overlay.viewChangedCallback = { x, y, w, h -> settingsService.saveViewSize(x, y, w, h) }
                        it.rcvDrawer.layoutManager = LinearLayoutManager(context)
                        it.rcvDrawer.adapter = Adapter(LayoutInflater.from(context))
                        it.vm = viewModel
                        activePlugin = pluginProvider.plugins[viewModel.selectedItem?.id]
                        it.executePendingBindings()
                        it.root
                    }


    private val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (propertyId == BR.selectedItem) {
                activePlugin = pluginProvider.plugins[viewModel.selectedItem?.id]
                binding?.drawer?.closeDrawers()
            }
        }
    }

    private var activePlugin: DebugPlugin? = null
        set(value) {
            if (field != value) {
                field?.destroy()
                field = value
                binding?.container?.removeAllViews()
                value?.apply {
                    binding?.container?.addView(create())
                    binding?.overlay?.setToolbarIcons(pluginIcons)
                }
            }
        }

    private class Adapter(inflater: LayoutInflater) : ObservableListBindingAdapter<DrawerItem>(inflater) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                DataBindingViewHolder(ItemDrawerBinding.inflate(inflater, viewGroup, false))

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: DrawerItem, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }

}

