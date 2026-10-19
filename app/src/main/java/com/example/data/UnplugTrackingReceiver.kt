package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * Manifest-registered receiver for ACTION_POWER_DISCONNECTED.
 *
 * Power-connected/disconnected broadcasts are exempt from Android 8+ background
 * broadcast restrictions, so this fires even when the app isn't running.
 *
 * On unplug it records a fresh discharge-session baseline, atomically:
 * - last_unplug_ms: when the charger was disconnected (for "Since charge")
 * - unplug_battery_pct: battery % at unplug (for the morning drain report)
 * - initial_battery_percent / initial_battery_ms: RESET so the drain-rate formula
 *   in DeviceTelemetryScanner measures discharge-only drain since unplug, never
 *   an average across charging sessions.
 *
 * This receiver is the SINGLE WRITER of these keys. Do not write them from
 * captureLiveTelemetry or any foreground transition detection: a stale
 * transition (app reopened days after unplug) would corrupt the baseline.
 */
class UnplugTrackingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_POWER_DISCONNECTED) return
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val pct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val now = System.currentTimeMillis()
        val editor = context
            .getSharedPreferences("battery_forensics_telemetry_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("last_unplug_ms", now)
        if (pct in 0..100) {
            editor.putInt("unplug_battery_pct", pct)
            // Reset the drain baseline: discharge-only rate from here on.
            editor.putInt("initial_battery_percent", pct)
            editor.putLong("initial_battery_ms", now)
        }
        editor.apply()
    }
}
