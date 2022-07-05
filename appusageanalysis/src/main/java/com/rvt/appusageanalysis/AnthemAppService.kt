package com.rvt.appusageanalysis

import androidx.annotation.Keep
import retrofit2.Response
import retrofit2.http.*

@Keep
interface AnthemAppService {

    @POST(APIConst.APP_USAGE_ANALYTIC_DATA)
    suspend fun sendAppUsageData(@Body data: AppUsageModel): Response<CommonResponse>
}