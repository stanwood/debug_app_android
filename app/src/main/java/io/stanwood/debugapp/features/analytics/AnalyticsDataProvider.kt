package io.stanwood.debugapp.features.analytics

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AnalyticsDataProvider @Inject constructor(val dataApi: DataApi) {

    private val events = mutableListOf<AnalyticsData>()
    private val analyticsData = BehaviorSubject.createDefault(events)

    val analyticsDataStream: Observable<MutableList<AnalyticsData>> =
            analyticsData.sample(200, TimeUnit.MILLISECONDS)


    private fun onNewData(data: DebugData) {
        data.intent.getStringExtra("data")?.split("|")
                ?.let {
                    when (it.get(1)) {
                        "exception" -> AnalyticsData(it.get(0).toLong(), "exception", stacktrace = if (it.size > 2) it[2] else "N/A")
                        else -> AnalyticsData(it.get(0).toLong(),
                                it.get(1),
                                if (it.size > 2) {
                                    val properties = mutableListOf<Pair<String, String>>()
                                    for (i in 2..(it.size - 2 / 2) step 2) {
                                        properties.add(Pair(it.get(i), it.get(i + 1)))
                                    }
                                    properties
                                } else null)
                    }
                }
                ?.apply {
                    events.add(this)
                    analyticsData.onNext(events)
                }
    }

    private var disposable: Disposable? = null

    init {
        subscribe()
    }

    fun clear() {
        disposable?.dispose()
        events.clear()
        analyticsData.onNext(events)
        subscribe()
    }

    private fun subscribe() {
        disposable?.dispose()
        disposable = dataApi.debugDataStream
                .filter {
                    it.source == "debugtracker"
                }
                .subscribeBy(onNext = {
                    onNewData(it)
                }, onError = { it.printStackTrace() })
    }
}


data class AnalyticsData(val timestamp: Long, val event: String, val properties: List<Pair<String, String>>? = null, val stacktrace: String? = null)