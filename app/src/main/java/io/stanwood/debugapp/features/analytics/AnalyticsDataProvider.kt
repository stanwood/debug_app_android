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

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.stanwood.debugapp.services.DataApi
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AnalyticsDataProvider @Inject constructor(private val dataApi: DataApi) {

    companion object {
        private const val MAX_ENTRIES = 1000
    }

    private val db = BehaviorSubject.createDefault(mutableListOf<AnalyticsData>())
    private val analyticsData = BehaviorSubject.createDefault(mutableListOf<AnalyticsData>())
    val analyticsDataStream: Observable<MutableList<AnalyticsData>> = analyticsData
    private var disposable: Disposable? = null

    init {
        subscribe()
    }

    fun clear() {
        db.onNext(mutableListOf())
    }

    private fun subscribe() {
        disposable?.dispose()
        disposable = db.switchMap { list ->
            dataApi.debugDataStream
                    .filter {
                        it.source == "debugtracker" && !it.intent.getStringExtra("data").isNullOrEmpty()
                    }
                    .map {
                        it.intent.getStringExtra("data").split("|")
                                .let {
                                    when (it.get(1)) {
                                        "exception" -> AnalyticsData(Date(it[0].toLong()), "exception", stacktrace = if (it.size > 2) it[2] else "N/A")
                                        else -> AnalyticsData(Date(it.get(0).toLong()),
                                                it.get(1),
                                                if (it.size > 2) {
                                                    val properties = mutableListOf<Pair<String, String>>()
                                                    for (i in 2..(it.size - 2 / 2) step 2) {
                                                        properties.add(Pair(it[i], it[i + 1]))
                                                    }
                                                    properties
                                                } else null)
                                    }
                                }
                    }
                    .buffer(500, TimeUnit.MILLISECONDS, 10)
                    .distinctUntilChanged()
                    .map {
                        list.addAll(it)
                        if (list.size >= MAX_ENTRIES) {
                            list.subList(0, MAX_ENTRIES / 2).clear()
                        }
                        list
                    }
        }
                .subscribeBy(onNext = {
                    analyticsData.onNext(it)
                }, onError = { it.printStackTrace() })
    }
}


