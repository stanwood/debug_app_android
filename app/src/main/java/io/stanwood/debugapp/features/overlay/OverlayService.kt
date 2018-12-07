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

package io.stanwood.debugapp.features.overlay


import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import dagger.android.AndroidInjection
import io.stanwood.debugapp.R
import io.stanwood.debugapp.services.SettingsRepository
import javax.inject.Inject


class OverlayService : AccessibilityService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var overlay: Overlay

    private var overlayView: View?=null

    private val windowManager by lazy {
        getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }


    companion object {
        private const val SERVICE_ID = 1224
        private const val NOTIFICATION_CHANNEL_ID = "stanwood_debug_view"
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {

    }

    override fun onInterrupt() {

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT))
        }
        val closeIntent = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(),
                Intent("io.stanwood.action.shutdown"), PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Happy debugging...")
                .addAction(R.drawable.ic_stop_black_24dp, "Cancel", closeIntent)
        startForeground(SERVICE_ID, builder.build())
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        overlayView= overlay.create().apply {
            windowManager.addView(this,
                    settingsRepository.getViewSize()
                            .let {
                                WindowManager.LayoutParams(
                                        it.width(),
                                        it.height(),
                                        it.left,
                                        it.top,
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                                        PixelFormat.TRANSLUCENT)
                                        .apply { gravity = Gravity.START or Gravity.TOP }
                            })
        }
        registerReceiver(receiver, IntentFilter("io.stanwood.action.shutdown"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        overlayView?.apply {
            this@OverlayService.windowManager.removeView(this)
            overlayView=null
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
        }
    }

}