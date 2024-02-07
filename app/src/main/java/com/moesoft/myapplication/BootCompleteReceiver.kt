package com.moesoft.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompleteReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val service = Intent(context, BlueListener::class.java)
                context?.startForegroundService(service)
            }
        }
    }
}