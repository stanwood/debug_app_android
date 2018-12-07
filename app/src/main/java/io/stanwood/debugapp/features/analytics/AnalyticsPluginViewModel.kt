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

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.stanwood.debugapp.databinding.ObservableArrayListEx
import io.stanwood.debugapp.features.HasViewType
import io.stanwood.debugapp.features.KeyValueViewModel
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnalyticsPluginViewModel @Inject constructor(private val analyticsDataProvider: AnalyticsDataProvider) {
    private val disposables = CompositeDisposable()
    val eventItems = ObservableArrayListEx<AnalyticsDataItemViewModel>()
    val detailItems = ObservableArrayListEx<HasViewType>()
    private var selectedEvent: AnalyticsDataItemViewModel? = null
    val callback: (AnalyticsDataItemViewModel) -> Unit = {
        selectedEvent = if (it != selectedEvent) it
                .apply {
                    selectedEvent?.selected = false
                    selected = true
                    analyticsData.stacktrace
                            ?.apply {
                                detailItems.swapItems(listOf(StacktraceViewModel(this)), null)
                            }
                            ?: mutableListOf(KeyValueViewModel("Event name", analyticsData.event),
                                    KeyValueViewModel("Event date", SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(analyticsData.timestamp)))
                                    .apply {
                                        analyticsData.properties?.map {
                                            KeyValueViewModel(it.first, it.second)
                                        }?.let {
                                            addAll(it)
                                        }
                                    }.toList()
                                    .apply {
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
                .sample(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    val newId = if (it.size > 0) it[0].timestamp.time else -1L
                    val currentId = if (eventItems.size > 0) eventItems[0].analyticsData.timestamp.time else -1L
                    if (newId != currentId) {
                        eventItems.swapItems(it
                                .map {
                                    AnalyticsDataItemViewModel(it, callback)
                                }.toList(), null)
                    } else {
                        if (it.size > eventItems.size) {
                            eventItems.addAll(it.subList(eventItems.size, it.size)
                                    .map {
                                        AnalyticsDataItemViewModel(it, callback)
                                    }.toList())
                        }
                    }
                }, onError = { it.printStackTrace() }))
    }

    fun clear() {
        eventItems.clear()
        selectedEvent = null
        detailItems.clear()
        analyticsDataProvider.clear()
    }

    fun destroy() {
        disposables.dispose()
    }


}