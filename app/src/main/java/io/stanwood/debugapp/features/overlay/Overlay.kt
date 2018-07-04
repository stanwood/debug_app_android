package io.stanwood.debugapp.features.overlay

import android.app.Application
import android.databinding.Observable
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.databinding.ItemDrawerBinding
import io.stanwood.debugapp.databinding.ViewOverlayBinding
import io.stanwood.debugapp.features.DebugPlugin
import io.stanwood.debugapp.features.PluginProvider
import io.stanwood.debugapp.services.SettingsRepository
import io.stanwood.framework.databinding.recyclerview.DataBindingViewHolder
import io.stanwood.framework.databinding.recyclerview.ObservableListBindingAdapter
import javax.inject.Inject

class Overlay @Inject constructor(private val context: Application,
                                  private val settingsRepository: SettingsRepository,
                                  private val viewModel: OverlayViewModel,
                                  private val pluginProvider: PluginProvider) {

    private var binding: ViewOverlayBinding? = null

    fun create(): View =
            ViewOverlayBinding.inflate(LayoutInflater.from(context))
                    .let {
                        binding = it
                        viewModel.addOnPropertyChangedCallback(propertyChangedCallback)
                        it.overlay.viewChangedCallback = { x, y, w, h -> settingsRepository.saveViewSize(x, y, w, h) }
                        it.overlay.expandStateChangedCallback = {
                            if (it) {
                                if (viewModel.selectedItem == null) {
                                    viewModel.selectedItem = viewModel.items[0]
                                }
                                activePlugin = viewModel.selectedItem?.let {
                                    pluginProvider.plugins[it.id]?.plugin
                                }
                            } else {
                                activePlugin = null
                            }
                        }
                        it.rcvDrawer.layoutManager = LinearLayoutManager(context)
                        it.rcvDrawer.adapter = Adapter(LayoutInflater.from(context))
                        it.vm = viewModel
                        it.executePendingBindings()
                        it.root
                    }


    private val propertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (propertyId == BR.selectedItem) {
                activePlugin = pluginProvider.plugins[viewModel.selectedItem?.id]?.plugin
                binding?.drawer?.closeDrawers()
            }
        }
    }

    private var activePlugin: DebugPlugin? = null
        set(value) {
            if (field != value) {
                field?.destroy()
                binding?.container?.removeAllViews()
                binding?.containerIcons?.removeAllViews()
                value?.apply {
                    binding?.container?.addView(create())
                    binding?.containerIcons?.addView(createToolbar())
                }
                field = value
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

