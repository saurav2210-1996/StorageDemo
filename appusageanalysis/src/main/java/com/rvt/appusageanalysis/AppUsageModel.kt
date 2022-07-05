package com.rvt.appusageanalysis

import com.google.gson.annotations.SerializedName

data class AppUsageModel(
    @SerializedName("device_id") var deviceId: String? = null,
    @SerializedName("app_usage") var appUsage: ArrayList<AppUsageData> = ArrayList<AppUsageData>(),
)
data class AppUsageData(
    @SerializedName("date") var date: String? = null,
    @SerializedName("app_name") var appName: String? = null,
    @SerializedName("package") var packageName: String? = null,
    @SerializedName("first_launch_time") var firstLaunchTime: String? = null,
    @SerializedName("last_launch_time") var lastLaunchTime: String? = null,
    @SerializedName("last_time_used") var lastTimeUsed: String? = null,
    @SerializedName("forground_usage") var forgroundUsage: Int? = 0,
    @SerializedName("total_time_visible") var totalTimeVisible: Int? = 0,
    @SerializedName("is_app") var isApp: Boolean? = null,
)