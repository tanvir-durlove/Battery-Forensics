package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Morning Drain Report (retention engine).
 *
 * Runs ~8am daily via WorkManager (exact time not guaranteed). Summarizes the overnight
 * discharge using ONLY real measurements:
 * - drop % = battery % at last unplug (recorded by UnplugTrackingReceiver) minus
 * current % (BatteryManager)
 * - top cause = the existing app-activity analysis (usage stats), when available
 *
 * Stays silent when data is insufficient (no unplug baseline, charging now, tiny drop).
 * Never invents numbers. No notification without POST_NOTIFICATIONS; the dashboard card
 * summary is still saved in that case.
 */
class MorningReportWorker(
    appContext: Context,
    params: WorkerParameters
): CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ctx.getSharedPreferences("battery_forensics_telemetry_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        // One report per day max.
        if (prefs.getString("morning_report_day", "") == today) return Result.success()

        val lastUnplugMs = prefs.getLong("last_unplug_ms", 0L)
        val unplugPct = prefs.getInt("unplug_battery_pct", -1)
        val nowMs = System.currentTimeMillis()
        if (lastUnplugMs <= 0L || unplugPct < 0 || nowMs - lastUnplugMs > 20 * 3_600_000L) {
            return Result.success()
}

        val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val nowPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?: -1
        if (nowPct < 0) return Result.success()
        if (isChargingNow(ctx)) return Result.success()

        val dropPct = unplugPct - nowPct
        if (dropPct < 2) {
            // Quiet night: save a calm summary for the dashboard card, no notification.
            saveSummary(prefs, today, "Quiet night — $dropPct% used since unplugged")
            return Result.success()
}

        val hours = ((nowMs - lastUnplugMs) / 3_600_000f).coerceAtLeast(0.5f)
        val rate = (dropPct / hours * 10f).roundToInt() / 10f
        val topCauses = findTopCauses(ctx)

        // Card headline stays clean; the notification keeps the top-1 cause inline.
        // The dashboard card renders the full top-3 from morning_report_top3.
        val cardHeadline = "Overnight: $dropPct% used (${rate}%/hr)"
        val notifyText = if (topCauses.isNotEmpty()) {
            "$cardHeadline · Top: ${topCauses[0]}"
        } else {
            cardHeadline
        }
        saveSummary(prefs, today, cardHeadline, topCauses)

        if (!notificationsGranted(ctx)) return Result.success()
        postNotification(ctx, notifyText)
        return Result.success()
}

    private fun isChargingNow(ctx: Context): Boolean {
        val intent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
}

    /**
     * Top-3 background consumers from the real usage-stats analysis, each formatted
     * like "Instagram background (~6%)". Empty when Usage Access isn't granted or no
     * data. Sorted by the app's established estimatedDrainPct (same numbers the
     * Drain Detective shows) — these are estimates, labeled with "~" throughout.
     */
    private fun findTopCauses(ctx: Context): List<String> {
        return try {
            val scanner = DeviceTelemetryScanner(ctx)
            if (!scanner.isUsageAccessGranted()) return emptyList()
            val db = ForensicsDatabase.getInstance(ctx)
            val repo = ForensicsRepository(db.forensicsDao(), scanner)
            val insights = repo.queryRealAppActivityInsights(scanner)
            insights.items
                .sortedByDescending { it.estimatedDrainPct }
                .take(3)
                .map { top ->
                    if (top.estimatedDrainPct > 0f) {
                        "${top.appName} background (~${top.estimatedDrainPct.roundToInt()}%)"
                    } else {
                        "${top.appName} background"
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveSummary(prefs: SharedPreferences, day: String, text: String, top3: List<String> = emptyList()) {
        prefs.edit()
            .putString("morning_report_day", day)
            .putString("morning_report_text", text)
            .putString("morning_report_top3", top3.take(3).joinToString("|"))
            .apply()
    }

    private fun notificationsGranted(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
} else {
            true
}
}

    private fun postNotification(ctx: Context, summary: String) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Morning battery report",
                    NotificationManager.IMPORTANCE_DEFAULT
)
)
}
        val openIntent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
}
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
.setSmallIcon(R.mipmap.ic_launcher)
.setContentTitle("Morning battery report")
.setContentText(summary)
.setStyle(NotificationCompat.BigTextStyle().bigText(summary))
.setContentIntent(pendingIntent)
.setAutoCancel(true)
.build()
        nm.notify(NOTIFICATION_ID, notification)
}

    companion object {
        private const val CHANNEL_ID = "morning_report"
        private const val NOTIFICATION_ID = 1001
        private const val UNIQUE_WORK_NAME = "morning_battery_report"

        /** Idempotent: KEEP policy preserves the original ~8am anchor across restarts. */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val nextRun = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
}
            val delayMs = (nextRun.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
            val request = PeriodicWorkRequestBuilder<MorningReportWorker>(24, TimeUnit.HOURS)
.setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
.addTag("morning_report")
.build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
)
}

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
}
}
}
