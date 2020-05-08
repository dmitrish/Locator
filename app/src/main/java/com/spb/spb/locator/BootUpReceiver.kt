package com.spb.spb.locator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.spb.spb.locator.BackgroundLocationService

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
            val sintent = Intent(context, BackgroundLocationService::class.java)
            context.startService(sintent)
        }
    }
}