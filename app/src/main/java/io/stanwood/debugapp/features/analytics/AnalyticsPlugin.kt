/*
 * Copyright (c) 2018 stanwood GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.stanwood.debugapp.features.analytics

import android.app.Application
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.databinding.ItemAnalyticsEventBinding
import io.stanwood.debugapp.databinding.ItemAnalyticsStacktraceBinding
import io.stanwood.debugapp.databinding.ItemKeyValueBinding
import io.stanwood.debugapp.databinding.ViewAnalyticsTrackerBinding
import io.stanwood.debugapp.databinding.ViewDefaultPluginToolbarBinding
import io.stanwood.debugapp.features.DebugPlugin
import io.stanwood.debugapp.features.HasViewType
import io.stanwood.framework.databinding.recyclerview.DataBindingViewHolder
import io.stanwood.framework.databinding.recyclerview.ObservableListBindingAdapter
import javax.inject.Inject

class AnalyticsPlugin @Inject constructor(val context: Application, private val analyticsDataProvider: AnalyticsDataProvider) : DebugPlugin {

    var binding: ViewAnalyticsTrackerBinding? = null

    override fun createToolbar(): View? {
        return ViewDefaultPluginToolbarBinding.inflate(LayoutInflater.from(context)).let {
            it.clearClickListener = View.OnClickListener { binding?.vm?.clear() }
            it.root
        }
    }

    override fun create(): View {
        return ViewAnalyticsTrackerBinding.inflate(LayoutInflater.from(context)).let {
            this@AnalyticsPlugin.binding = it
            it.rcvEvents.apply {
                layoutManager = LinearLayoutManager(context)
                        .apply { stackFromEnd = true }
                adapter = EventsAdapter(LayoutInflater.from(context))
                        .apply {
                            registerAdapterDataObserver(scrollToBottomDataObserver)
                        }
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
            it.rcvDetails.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = DetailsAdapter(LayoutInflater.from(context))

            }
            it.vm = AnalyticsPluginViewModel(analyticsDataProvider)
            it.root
        }
    }

    private val scrollToBottomDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            (binding?.rcvEvents?.layoutManager as LinearLayoutManager).apply {
                val lastVisiblePos = findLastVisibleItemPosition()
                if (lastVisiblePos >= positionStart - 1) {
                    scrollToPositionWithOffset(positionStart, 0)
                }
            }
        }
    }

    override fun destroy() {
        binding?.apply {
            rcvEvents.adapter?.unregisterAdapterDataObserver(scrollToBottomDataObserver)
            vm?.destroy()
            unbind()
        }
    }


    private class EventsAdapter(inflater: LayoutInflater) : ObservableListBindingAdapter<AnalyticsDataItemViewModel>(inflater) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                DataBindingViewHolder(ItemAnalyticsEventBinding.inflate(inflater, viewGroup, false))

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: AnalyticsDataItemViewModel, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }

    private class DetailsAdapter(inflater: LayoutInflater) : ObservableListBindingAdapter<HasViewType>(inflater) {


        override fun getItemViewType(position: Int, item: HasViewType?) = item?.viewType ?: 0
        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                when (viewType) {
                    StacktraceViewModel.VIEWTYPE -> DataBindingViewHolder(ItemAnalyticsStacktraceBinding.inflate(inflater, viewGroup, false))
                    else -> DataBindingViewHolder(ItemKeyValueBinding.inflate(inflater, viewGroup, false))
                }

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: HasViewType, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }
}