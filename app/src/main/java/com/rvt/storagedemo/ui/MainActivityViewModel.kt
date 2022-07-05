package com.rvt.storagedemo.ui

import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rvt.storagedemo.repository.CountryRepository
import com.rvt.storagedemo.services.CountryService
import com.rvt.storagedemo.ui.helper.FileObserver
import com.rvt.storagedemo.utils.DownloadFileFromURLTask
import com.rvt.storagedemo.utils.DownloadListener
import com.rvt.storagedemo.utils.Helper
import kotlinx.coroutines.*
import retrofit2.await
import retrofit2.awaitResponse
import java.io.File


const val outputDir = "StorageApp"
enum class State{
    DELETE,
    CREATE
}

class MainActivityViewModel(
    private val context: Context,
    private val countryRepository: CountryRepository
) : ViewModel() {

    private val _fileState = MutableLiveData<State>()
    val currentState : LiveData<State> = _fileState

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                //callCountryApi()
            }
        }
    }


    fun downloadFile(mContext: Context) {
        val download = DownloadFileFromURLTask(mContext, outputDir, object : DownloadListener {
            override fun onSuccess(path: String) {
                _fileState.postValue(State.CREATE)
                Toast.makeText(
                    mContext,
                    "File is downloaded successfully at $path",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(error: String) {
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show()
            }
        })
        download.execute()
    }

    fun readFiles() : String{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val destURL = Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + outputDir
            //val destURL = Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS
            if(File(destURL).exists()){
                val files = File(destURL).listFiles()
                var fileName:String = ""
                files?.forEach {
                    fileName += it.name+"\n"
                }
                return if(fileName.isEmpty()) "Nothing" else fileName
            }
            return "Nothing"
        }else{
            val dir = File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), outputDir)
            //val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if(dir.exists()){
                val files = dir.listFiles()
                var fileName:String = ""
                files?.forEach {
                    fileName += it.name+"\n"
                }
                return if(fileName.isEmpty()) "Nothing" else fileName
            }
            return "Nothing"

        }
    }

    fun deleteFile(launcher: ActivityResultLauncher<IntentSenderRequest>){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val destURL = Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + outputDir
            //val destURL = Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS
            if(File(destURL).exists()){
                val files = File(destURL).listFiles()
                kotlin.run stop@{
                    files?.forEach {
                        if(it.name == "1.jpg"){
                            it.delete()
                            _fileState.postValue(State.DELETE)
                            return@stop
                        }
                    }
                }
            }
        }else{
            val dir = File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), outputDir)
            //val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if(dir.exists()){
                val files = dir.listFiles()
                kotlin.run stop@{
                    files?.forEach {
                        if(it.name == "1.jpg"){
                            createDelete(it, launcher)
                            return@stop
                        }
                    }
                }
            }
        }
    }

    private fun createDelete(it: File?, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        try {


            val mediaID = Helper.getFilePathToMediaID(it!!.path, context)
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri("external"),
                mediaID
            )
            //delete object using resolver
            context.contentResolver.delete(uri/*Uri.fromFile(it)*/, null, null)
        } catch (e: Exception) {
            var pendingIntent: PendingIntent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val collection: ArrayList<Uri> = ArrayList()
                //collection.add(Uri.fromFile(it))
                val mediaID = Helper.getFilePathToMediaID(it!!.path, context)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.getContentUri("external"),
                    mediaID
                )
                collection.add(uri)

                pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, collection)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e is RecoverableSecurityException) {
                    pendingIntent = e.userAction.actionIntent
                }
            }
            if (pendingIntent != null) {
                val sender = pendingIntent.intentSender
                val request = IntentSenderRequest.Builder(sender).build()
                launcher.launch(request)
            }
        }

    }


    private suspend fun callCountryApi(){
        val request = countryRepository.getCountryList()
        Log.e("TAG", "response : ${request.data.size}")
    }
}