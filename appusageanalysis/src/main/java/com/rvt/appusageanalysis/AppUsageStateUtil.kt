package com.rvt.appusageanalysis

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppUsageStateUtil(private val context: Context){

    fun setupAppStatisticSchedules(
        hourToStart: Int,
        minuteToStart: Int,
    ) {
        if(!checkUsageStatsPermission()){
            context.startActivity(Intent(context,PermissionActivity::class.java).apply {
                putExtra("hourToStart",hourToStart)
                putExtra("minuteToStart",minuteToStart)
            })
            return
        }

        require(hourToStart in 0..24) {
            "setupSchedules: hourToStart needs to be between 0 and 24."
        }
        require(minuteToStart in 0..60) {
            "setupSchedules: minuteToStart needs to be between 0 and 60."
        }

        Log.d(
            "TAG",
            "Download scheduled to start at $hourToStart:${
                minuteToStart.toString().padStart(2, '0')
            }"
        )
        scheduleWork(
            hourToStart,
            minuteToStart,

            )
    }

    fun checkUsageStatsPermission(): Boolean {
        val appOpsManager = context.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
        // `AppOpsManager.checkOpNoThrow` is deprecated from Android Q
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun scheduleWork(hourToStart: Int, minuteToStart: Int) {

        val flexTime = calculateFlex(hourToStart, minuteToStart)

        val myConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AppUsageStateWorker>(
            1.toLong(),
            TimeUnit.DAYS,
            flexTime,
            TimeUnit.MILLISECONDS)
            .setConstraints(myConstraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateFlex(hourOfTheDay: Int, minute: Int): Long {

        // Initialize the calendar with today and the preferred time to run the job.
        val cal1 = Calendar.getInstance()
        cal1[Calendar.HOUR_OF_DAY] = hourOfTheDay
        cal1[Calendar.MINUTE] = minute
        cal1[Calendar.SECOND] = 0

        val cal2 = Calendar.getInstance()
        if (cal2.timeInMillis < cal1.timeInMillis) {
            cal2.timeInMillis = cal2.timeInMillis + TimeUnit.DAYS.toMillis(1.toLong())
        }
        val delta = cal2.timeInMillis - cal1.timeInMillis
        return if (delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) delta else PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
    }

    companion object {
        const val WORK_TAG = "APP_USAGE_STATE_WORK_TAG"
        const val WORK_STOP_TAG = "WORK_STOP_TAG"
    }
}