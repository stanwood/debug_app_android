package io.stanwood.debugapp.features.analytics

import android.app.Application
import android.content.Intent
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.stanwood.debugapp.BR
import io.stanwood.debugapp.R
import io.stanwood.debugapp.databinding.ItemAnalyticsEventBinding
import io.stanwood.debugapp.databinding.ItemAnalyticsKeyValueBinding
import io.stanwood.debugapp.databinding.ViewAnalyticsTrackerBinding
import io.stanwood.debugapp.features.DebugPlugin
import io.stanwood.framework.databinding.recyclerview.DataBindingViewHolder
import io.stanwood.framework.databinding.recyclerview.ObservableListBindingAdapter
import javax.inject.Inject

class AnalyticsPlugin @Inject constructor(val context: Application, private val analyticsDataProvider: AnalyticsDataProvider) : DebugPlugin {
    override fun onToolbarIconClicked(position: Int) {

    }

    override val pluginIcons: Array<Int>?
        get() = arrayOf(R.drawable.ic_clear_all_black_24dp)

    var binding: ViewAnalyticsTrackerBinding? = null

    override fun create(): View {
        return ViewAnalyticsTrackerBinding.inflate(LayoutInflater.from(context)).let {
            this@AnalyticsPlugin.binding = it
            it.rcvEvents.apply {
                layoutManager = LinearLayoutManager(context)
                        .apply { stackFromEnd = true   }
                adapter = EventsAdapter(LayoutInflater.from(context))
                        .apply {
                            registerAdapterDataObserver(scrollToBottomDataObserver)
                        }
                addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
            }
            it.rcvDetails.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = DetailsAdapter(LayoutInflater.from(context))
                addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
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

    private class EventsAdapter(inflater: LayoutInflater) : ObservableListBindingAdapter<AnalyticsDataViewModel>(inflater) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                DataBindingViewHolder(ItemAnalyticsEventBinding.inflate(inflater, viewGroup, false))

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: AnalyticsDataViewModel, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }

    private class DetailsAdapter(inflater: LayoutInflater) : ObservableListBindingAdapter<KeyValueViewModel>(inflater) {

        override fun onCreateViewHolder(inflater: LayoutInflater, viewGroup: ViewGroup, viewType: Int) =
                DataBindingViewHolder(ItemAnalyticsKeyValueBinding.inflate(inflater, viewGroup, false))

        override fun bindItem(holder: DataBindingViewHolder<*>, position: Int, item: KeyValueViewModel, payloads: MutableList<Any>?) {
            holder.binding.setVariable(BR.vm, item)
        }
    }

    fun onNewData(intent: Intent) {

        val data = intent.getStringExtra("data")
        val appId = intent.getStringExtra("appid")
        /*view.addRow(JSONObject(data)
                .let {
                    AnalyticsDataViewModel(dateFormat.format(Date(it.getLong("time"))),
                            it.getStringOrDefault("eventname"),
                            it.getStringOrDefault("name"),
                            it.getStringOrDefault("itemid"))
                })*/
    }
}