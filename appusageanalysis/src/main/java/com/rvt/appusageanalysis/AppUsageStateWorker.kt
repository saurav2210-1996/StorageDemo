package com.rvt.appusageanalysis

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AppUsageStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageStatisticsRepository: UsageStatisticsRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appUsage = usageStatisticsRepository.getUsageStatistics()
        val status = usageStatisticsRepository.uploadAppUsageStatistics(appUsage)
        return if(status) Result.success() else Result.retry()
    }
}