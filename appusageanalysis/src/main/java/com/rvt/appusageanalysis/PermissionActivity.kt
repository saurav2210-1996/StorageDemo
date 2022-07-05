package com.rvt.appusageanalysis

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PermissionActivity : AppCompatActivity() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var appUsageStateUtil: AppUsageStateUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (!appUsageStateUtil.checkUsageStatsPermission()) {
                    resultLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } else {
                    appUsageStateUtil.setupAppStatisticSchedules(
                        intent.getIntExtra("hourToStart", 0),
                        intent.getIntExtra("minuteToStart", 0)
                    )
                    finish()
                }
            }

        resultLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}