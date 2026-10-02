package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class LiveBatteryBroadcastEvent(
    val action: String,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val chargingSource: String,
    val temperatureCelsius: Float,
    val voltageVolts: Float,
    val currentMilliAmps: Int,
    val powerWatts: Float?,
    val timestampMs: Long,
    val timeFormatted: String
)

/**
 * BroadcastReceiver that listens for [Intent.ACTION_BATTERY_CHANGED],
 * [Intent.ACTION_POWER_CONNECTED], and [Intent.ACTION_POWER_DISCONNECTED]
 * to track live charging and discharging events.
 */
class BatteryTrackingReceiver(
    private val onBatteryEvent: (LiveBatteryBroadcastEvent) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action ?: Intent.ACTION_BATTERY_CHANGED
        if (
            action != Intent.ACTION_BATTERY_CHANGED &&
            action != Intent.ACTION_POWER_CONNECTED &&
            action != Intent.ACTION_POWER_DISCONNECTED
        ) {
            return
        }

        val event = parseBatteryIntent(context, intent)
        onBatteryEvent(event)
    }

    companion object {
        fun createIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
        }

        fun register(context: Context, receiver: BatteryTrackingReceiver): Intent? {
            val stickyIntent = ContextCompat.registerReceiver(
                context,
                receiver,
                createIntentFilter(),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            if (stickyIntent != null) {
                receiver.onReceive(context, stickyIntent)
            }
            return stickyIntent
        }

        fun unregister(context: Context, receiver: BatteryTrackingReceiver) {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }

        fun parseBatteryIntent(context: Context, intent: Intent): LiveBatteryBroadcastEvent {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            val percent = if (level >= 0) {
                ((level * 100f) / scale).roundToInt().coerceIn(0, 100)
            } else {
                val bmPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
                bmPct.coerceIn(0, 100)
            }

            val status = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_DISCHARGING
            )
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val action = intent.action ?: Intent.ACTION_BATTERY_CHANGED

            val isCharging = when (action) {
                Intent.ACTION_POWER_CONNECTED -> true
                Intent.ACTION_POWER_DISCONNECTED -> false
                else -> status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL ||
                    plugged != 0
            }

            val chargingSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "Charging (AC)"
                BatteryManager.BATTERY_PLUGGED_USB -> "Charging (USB)"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Charging (Wireless)"
                else -> if (isCharging) "Charging" else "Discharging"
            }

            val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val tempCelsius = if (tempTenths > 0) {
                ((tempTenths / 10f) * 10f).roundToInt() / 10f
            } else 0f

            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val voltageVolts = when {
                voltageMv > 1000 -> ((voltageMv / 1000f) * 100f).roundToInt() / 100f
                voltageMv in 1..15 -> voltageMv.toFloat()
                else -> 0f
            }

            val extraCurrent = intent.getIntExtra("current_now", Int.MIN_VALUE)
            val rawCurrent = if (extraCurrent != Int.MIN_VALUE) {
                extraCurrent
            } else {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
            }
            val currentMa = when {
                rawCurrent == 0 || rawCurrent == Int.MIN_VALUE -> 0
                abs(rawCurrent) > 10_000 -> rawCurrent / 1000
                else -> rawCurrent
            }

            val watts = if (voltageVolts > 0f && currentMa != 0) {
                ((abs(voltageVolts * currentMa) / 1000f) * 10f).roundToInt() / 10f
            } else null

            val now = System.currentTimeMillis()
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(now))

            return LiveBatteryBroadcastEvent(
                action = action,
                batteryPercent = percent,
                isCharging = isCharging,
                chargingSource = chargingSource,
                temperatureCelsius = tempCelsius,
                voltageVolts = voltageVolts,
                currentMilliAmps = currentMa,
                powerWatts = watts,
                timestampMs = now,
                timeFormatted = timeStr
            )
        }
    }
}
