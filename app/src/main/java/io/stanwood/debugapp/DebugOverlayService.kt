package io.stanwood.debugapp


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
import android.view.*
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class DebugOverlayService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingWidget: OverlayView? = null
    private val receiver = Receiver()
    private val defaultLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT)
    private val widgetLayoutParams
        get() = floatingWidget?.layoutParams as WindowManager.LayoutParams

    companion object {
        private val SERVICE_ID = 1224
        private val NOTIFICATION_CHANNEL_ID = "stanwood_debug_view"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        floatingWidget = (LayoutInflater.from(this).inflate(R.layout.view_debugoverlay, null) as OverlayView)
                .apply {
                    findViewById<View>(R.id.btn).setOnTouchListener(OverlayTouchListener(context))
                }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(floatingWidget, defaultLayoutParams.apply { gravity = Gravity.START or Gravity.TOP })
        registerReceiver(receiver, IntentFilter("io.stanwood.action.log.tracker").apply { addAction("io.stanwood.action.shutdown") })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        floatingWidget?.apply {
            windowManager?.removeView(this)
        }
    }

    private inner class OverlayTouchListener(context: Context) : View.OnTouchListener {
        private var viewPosition = intArrayOf(0, 0)
        private var touchPosition = floatArrayOf(0f, 0f)
        private val touchSlope = ViewConfiguration.get(context).scaledTouchSlop
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val params = widgetLayoutParams
                    viewPosition[0] = params.x
                    viewPosition[1] = params.y
                    touchPosition[0] = event.rawX
                    touchPosition[1] = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val params = widgetLayoutParams
                    params.x = viewPosition[0] + (event.rawX - touchPosition[0]).toInt()
                    params.y = viewPosition[1] + (event.rawY - touchPosition[1]).toInt()
                    windowManager?.updateViewLayout(floatingWidget, params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val Xdiff = Math.abs(event.rawX - touchPosition[0]).toInt()
                    val Ydiff = Math.abs(event.rawY - touchPosition[1]).toInt()
                    if (Xdiff < touchSlope && Ydiff < touchSlope) {
                        view.performClick()
                    }
                    return true
                }
                else -> return false
            }
        }
    }

    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "io.stanwood.action.log.tracker") {
                val data = intent.getStringExtra("data")
                val appId = intent.getStringExtra("appid")
                floatingWidget?.addRow(JSONObject(data)
                        .let {
                            Row(SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(Date(it.getLong("time"))),
                                    it.getStringOrDefault("eventname"),
                                    it.getStringOrDefault("name"),
                                    it.getStringOrDefault("itemid"))
                        })
            } else if (intent.action == "io.stanwood.action.shutdown") {
                stopSelf()
            }
        }
    }

    private fun JSONObject.getStringOrDefault(name: String, default: String = "") = if (has(name)) getString(name) else default
}