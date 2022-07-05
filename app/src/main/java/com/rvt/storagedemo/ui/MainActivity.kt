package com.rvt.storagedemo.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import com.rvt.appusageanalysis.AppUsageStateUtil
import com.rvt.storagedemo.R
import com.rvt.storagedemo.customview.ScoreCardView
import com.rvt.storagedemo.ui.helper.FileObserver
import com.rvt.storagedemo.ui.helper.PathState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private val mainActivityViewModel : MainActivityViewModel by viewModel()
    private val fileObserver : FileObserver by inject()

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val appUsageStateUtil : AppUsageStateUtil by inject()

    companion object {
        private val SDK_28_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R/*Q*/
        private val PERMISSIONS_LEGACY = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private val PERMISSIONS_SCOPED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private val PERMISSIONS = if (SDK_28_ABOVE) PERMISSIONS_SCOPED else PERMISSIONS_LEGACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // request permissions
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val ungrantList = permissions.entries.filter { it.value == false }
            permissions.entries.forEach {
                Log.e("TAG", "${it.key} = ${it.value}")
            }
        }

        deleteLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                btnRead.performClick()
            }
        }

        //checkPermissions()
        //setOnClickListener()
        //setObservable()

        //unzip()
        //test()

        btnSchedule.setOnClickListener {
            appUsageStateUtil.setupAppStatisticSchedules(13,36)
        }
    }

    private fun unzip() {

        //val path = Environment.DIRECTORY_DOWNLOADS + File.separator + "Options OI"
        val path = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            File.separator + "Options OI" + File.separator + "07JUN2022.zip"
        )
        val destPath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            File.separator + "Options OI"
        )


        //val dirPath = File(filesDir, "my_docs")
        val dirPath = getDir("my_docs1", Context.MODE_PRIVATE)


        val year = Calendar.getInstance().get(Calendar.YEAR)
        val todayDate = "07JUN2022"//Utility.getDate("ddMMMYYYY").toUpperCase()
        val month = "JUN"
        val url = "https://www1.nseindia.com/content/historical/DERIVATIVES/$year/$month/fo${todayDate}bhav.csv.zip"


        //UnzipUtils.unzip(File(filesDir, "my_docs" + File.separator + "$todayDate.zip"),dirPath.path)


        /*PRDownloader.download(url, dirPath.path, "$todayDate.zip")
            .build()
            .setOnStartOrResumeListener { }
            .setOnPauseListener { }
            .setOnCancelListener {
                Log.e("TAG","setOnCancelListener")
            }
            .setOnProgressListener { }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Log.e("TAG","onDownloadComplete")
                }
                override fun onError(error: com.downloader.Error?) {
                    Log.e("TAG","onError ${error.toString()}")
                }
            })*/

        GlobalScope.launch {
            delay(1000)

            Log.e("TAG","onDownloadComplete $dirPath : ${dirPath.exists()}")

            Log.e("TAG","Start ${dirPath.listFiles()?.size ?: 0}")
            dirPath.listFiles()?.let {
                it.forEach { file ->
                    Log.e("TAG","File : ${file.path} ")
                }
            }

            /*dirPath.listFiles()?.let {
                it.forEach { file ->
                    file.delete()
                }
            }*/

            Log.e("TAG","End ${dirPath.listFiles()?.size ?: 0}")
        }

    }



    private fun test(){
        /*scoreCardView1.setParData(listOf(
            "1","2","3","4","5","6","7","8","9","0"
        ))*/
        scoreCardView1.setDataSource(
            listOf(
                listOf("SCORE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                //listOf("SCORE","--","--","--","--","--","--","--","--","--","--")
            )
        )
        scoreCardView1?.setSelectionIndex(0, 1)

        scoreCardView1.setOnScoreCardListener(object : ScoreCardView.ScoreCardListener {
            override fun onCellClicked(viewId: Int, rowIndex: Int, colIndex: Int) {
                Log.e("TAG", "onCellClicked : ${viewId == scoreCardView1.id}")
            }

            override fun onEditPlayer(viewId: Int, rowIndex: Int, colIndex: Int) {
                Log.e("TAG", "viewId : ${viewId == scoreCardView1.id}")
            }

        })
    }

    private fun setObservable() {

        mainActivityViewModel.currentState.observe(this, Observer {
            btnRead.performClick()
        })

        fileObserver.startWatching()
        fileObserver.fileState.distinctUntilChanged().observe(this, {
            when (it) {
                is PathState.Create -> {
                    btnRead.performClick()
                    Log.e("TAG", "Create")
                }
                is PathState.Delete -> {
                    btnRead.performClick()
                    Log.e("TAG", "Delete")
                }
                else -> {
                    Log.e("TAG", "else")
                }
            }
        })
    }

    private fun setOnClickListener() {

        btnRead.setOnClickListener {
            tvReadData.text = mainActivityViewModel.readFiles()
        }

        btnDelete.setOnClickListener {
            mainActivityViewModel.deleteFile(deleteLauncher)
        }

        btnSave.setOnClickListener {
            mainActivityViewModel.downloadFile(this)
        }

    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            requestPermissions()
        }
    }

    private fun hasPermissions() = PERMISSIONS.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        permissionsLauncher.launch(PERMISSIONS)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("TAG", "onConfigurationChanged")
    }
}