package io.stanwood.debugapp.features.analytics

import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject


class AnalyticsDataProvider @Inject constructor(dataApi: DataApi) {

    private val events = mutableListOf<AnalyticsData>()
    private val analyticsData = BehaviorSubject.createDefault(events)

    val analyticsDataStream: Observable<MutableList<AnalyticsData>> = analyticsData

    private fun onNewData(data: DebugData) {
        data.intent.getStringExtra("data")?.split("|")
                ?.let {
                    AnalyticsData(it.get(0).toLong(),
                            it.get(1),
                            if (it.size > 2) {
                                val properties = mutableListOf<Pair<String, String>>()
                                for (i in 2..(it.size - 2 / 2) step 2) {
                                    properties.add(Pair(it.get(i), it.get(i + 1)))
                                }
                                properties
                            } else null)
                }
                ?.apply {
                    events.add(this)
                    analyticsData.onNext(events)
                }
    }

    init {
        dataApi.debugDataStream
                .filter {
                    it.source == "debugtracker"
                }
                .subscribeBy(onNext = {
                    onNewData(it)
                }, onError = { it.printStackTrace() })
    }
}


data class AnalyticsData(val timestamp: Long, val event: String, val properties: List<Pair<String, String>>? = null)