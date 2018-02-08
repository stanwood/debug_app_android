package io.stanwood.debugapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_DRAW_OVER = 1337
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasOverlayPermission) {
            startService()
            finish()
        } else {
            setContentView(R.layout.activity_main)
            findViewById<Button>(R.id.btn).apply {
                setText("Request permission")
                setOnClickListener(View.OnClickListener { requestOverlayPermission() })
            }
        }
    }

    val hasOverlayPermission
        get() = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)))

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
        startActivityForResult(intent, REQUEST_CODE_DRAW_OVER)
    }

    private fun startService() {
        val serviceIntent = Intent(this, DebugOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_DRAW_OVER) {
            if (hasOverlayPermission) {
                startService()
                finish()
            } else {
                Toast.makeText(this, "Error getting permission", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

