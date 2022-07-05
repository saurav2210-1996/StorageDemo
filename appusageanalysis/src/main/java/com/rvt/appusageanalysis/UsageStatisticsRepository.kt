package com.rvt.appusageanalysis

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class UsageStatisticsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val anthemAppService: AnthemAppService,
) {

    companion object {
        const val ONE_DAY_MILLI_SEC = 1000 * 60 * 60 * 24
    }

    fun getUsageStatistics() : AppUsageModel{

        val cal: Calendar = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.time = Date(Date().time - ONE_DAY_MILLI_SEC)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = cal.timeInMillis + ONE_DAY_MILLI_SEC - 1


        val appUsage = AppUsageModel()
        val list = ArrayList<AppUsageData>()
        val useStateManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val aggregatedStatsMap: Map<String, UsageStats> = useStateManager.queryAndAggregateUsageStats(start, end)
        val packageManager: PackageManager = context.applicationContext.packageManager
        for ((k, v) in aggregatedStatsMap) {
            val data = AppUsageData()
            try {
                data.appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(
                        v.packageName,
                        PackageManager.GET_META_DATA
                    )
                ) as String
            }catch (e : Exception){
                data.packageName = v.packageName
            }

            data.firstLaunchTime = getDateTimeFromTimeStamp(v.firstTimeStamp)
            data.lastLaunchTime = getDateTimeFromTimeStamp(v.lastTimeStamp)
            data.date = getDateTimeFromTimeStamp(System.currentTimeMillis())
            data.lastTimeUsed = getDateTimeFromTimeStamp(v.lastTimeUsed)
            data.isApp = true
            list.add(data)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                data.forgroundUsage = v.totalTimeForegroundServiceUsed.toInt()
                data.totalTimeVisible = v.totalTimeVisible.toInt()

                println(
                    "$k = ${v.firstTimeStamp} ${v.lastTimeStamp} ${v.lastTimeUsed} " +
                            "${v.totalTimeForegroundServiceUsed} ${v.totalTimeVisible}"
                )
            }
        }

        appUsage.deviceId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        appUsage.appUsage = list
        return appUsage

    }

    //from Android R we can’t run this method when the device is locked.
    fun getNonSystemAppsList(): Map<String, String> {
        val appInfos = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appInfoMap = HashMap<String, String>()
        for (appInfo in appInfos) {
            if (appInfo.flags != ApplicationInfo.FLAG_SYSTEM) {
                appInfoMap[appInfo.packageName] =
                    context.packageManager.getApplicationLabel(appInfo).toString()
            }
        }
        return appInfoMap

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val userManager = context.getSystemService( Context.USER_SERVICE ) as UserManager
            if ( userManager.isUserUnlocked ) {
                // Access usage history ...
            }
        }*/
    }

    suspend fun uploadAppUsageStatistics(data: AppUsageModel) : Boolean{
        return withContext(Dispatchers.IO) {
            val response = anthemAppService.sendAppUsageData(data)
            if(response.isSuccessful){
                Log.e("TAG", "message : ${response.body()?.message}")
                Log.e("TAG", "status : ${response.body()?.status}")
                true
            }else false
        }
    }
}