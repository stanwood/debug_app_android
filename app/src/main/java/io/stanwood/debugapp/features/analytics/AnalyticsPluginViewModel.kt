package io.stanwood.debugapp.features.analytics

import android.databinding.ObservableArrayList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.stanwood.debugapp.ItemSwapHelper
import io.stanwood.debugapp.ObservableArrayListEx
import javax.inject.Inject

class AnalyticsPluginViewModel @Inject constructor(private val analyticsDataProvider: AnalyticsDataProvider) {
    private val disposables = CompositeDisposable()
    val eventItems = ObservableArrayList<AnalyticsDataViewModel>()
    val detailItems = ObservableArrayListEx<HasViewType>()
    private var selectedEvent: AnalyticsDataViewModel? = null
    val callback: (AnalyticsDataViewModel) -> Unit = {
        selectedEvent = if (it != selectedEvent) it
                .apply {
                    selectedEvent?.selected = false
                    selected = true
                    analyticsData.stacktrace
                            ?.apply {
                                detailItems.swapItems(listOf(StacktraceViewModel(this)), null)
                            }
                            ?: analyticsData.properties
                                    ?.map {
                                        KeyValueViewModel(it.first, it.second)
                                    }
                                    ?.toList()
                                    ?.apply {
                                        detailItems.swapItems(this, null)
                                    }
                }
        else it.let {
            it.selected = false
            detailItems.clear()
            null
        }
    }

    init {
        disposables.add(analyticsDataProvider.analyticsDataStream
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    object : ItemSwapHelper<AnalyticsDataViewModel, AnalyticsData>() {
                        override fun mapAll(newItems: List<AnalyticsData>) = newItems.map {
                            AnalyticsDataViewModel(it, callback)
                        }
                                .toList()

                        override fun isSame(item: AnalyticsDataViewModel, newItem: AnalyticsData) = item.analyticsData == newItem

                        override fun map(item: AnalyticsData) = AnalyticsDataViewModel(item, callback)
                    }
                            .swap(eventItems, it)


                }, onError = { it.printStackTrace() }))
    }

    fun onToolbarAction(position: Int) {
        selectedEvent = null
        detailItems.clear()
        analyticsDataProvider.clear()
    }

    fun destroy() {
        disposables.dispose()
    }


}