package io.stanwood.debugapp.features.analytics

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataApi @Inject constructor(context: Application) : BroadcastReceiver() {
    private val debugData = PublishSubject.create<DebugData>()
    val debugDataStream: Observable<DebugData> = debugData

    init {
        context.registerReceiver(this, IntentFilter("io.stanwood.debugapp.plugin"))
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra("source")?.apply {
            debugData.onNext(DebugData(this, intent))
        }
    }
}

data class DebugData(val source: String, val intent: Intent)