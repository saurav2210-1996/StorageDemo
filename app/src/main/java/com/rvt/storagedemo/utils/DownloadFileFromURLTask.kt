package com.rvt.storagedemo.utils

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


const val srcURL = "https://media.istockphoto.com/photos/vegetable-garden-in-canada-cucuzza-vine-on-red-brick-wall-16x9-format-picture-id1334919554"
const val fileName = "1.jpg"

lateinit var destURL: String


private lateinit var outputPath: String
private lateinit var error: String


class DownloadFileFromURLTask(
    val mContext: Context,
    private val outputDir: String,
    private val downloadListener: DownloadListener
) : AsyncTask<String?, Int?, Boolean>() {

    private var progressDialog: ProgressDialog? = null
    private val TAG = javaClass.simpleName

    init {
        // initialization of ProgressDialog
        progressDialog = ProgressDialog(mContext)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog!!.setCancelable(false)
        progressDialog!!.setTitle("Downloading")
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg p0: String?): Boolean {

        try {
            var total: Long = 0
            var count = 0
            val url = URL(srcURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val buffer = ByteArray(1024)
            // getting file length
            val lenghtOfFile = connection.contentLength

            // input stream to read file
            val inputStream = connection.inputStream
            // Output stream to write file
            val outputStream: OutputStream?

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

                destURL = Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + outputDir
                var desFile = File(destURL)
                if (!desFile.exists()) {
                    desFile.mkdir()
                }

                destURL = destURL + File.separator + fileName
                // final output path
                outputPath = destURL

                outputStream = FileOutputStream(destURL)
                while (inputStream.read(buffer).also { count = it } != -1) {
                    total += count.toLong()
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress((total * 100 / lenghtOfFile).toInt())

                    // writing data to file
                    outputStream.write(buffer, 0, count)
                }
                // flushing output
                outputStream.flush()
                // closing streams
                outputStream.close()
                inputStream.close()
            } else {

                Log.i(TAG, "Downloading -> Android 11 or Above")
                val bis = BufferedInputStream(inputStream)
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)

                var desDirectory = Environment.DIRECTORY_DOWNLOADS
                // If you want to create custom directory inside Download directory only
                desDirectory = desDirectory + File.separator + outputDir
                val desFile = File(desDirectory)
                if (!desFile.exists()) {
                    desFile.mkdir()
                }
                // final output path
                outputPath = desDirectory + File.separator + fileName
                Log.i(TAG, "Downloading destination directory: $desDirectory")

                values.put(MediaStore.MediaColumns.RELATIVE_PATH, desDirectory)
                //  values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val uri = mContext.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    outputStream = mContext.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        val bos = BufferedOutputStream(outputStream)
                        var bytes = bis.read(buffer)
                        while (bytes.also { count = it } != -1) {
                            total += count.toLong()
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            publishProgress((total * 100 / lenghtOfFile).toInt())
                            // writing data to file
                            bos.write(buffer, 0, count)
                            bos.flush()
                            bytes = bis.read(buffer)
                        }
                        bos.close()
                    }
                }
                bis.close()
            }

            return !isCancelled
        } catch (e: Exception) {
            error = e.toString()
            Log.e(TAG, "Exception: $e")
        }

        return false
    }

    override fun onPostExecute(result: Boolean?) {
        progressDialog!!.dismiss()
        if (result!!) {
            downloadListener.onSuccess(outputPath)
        } else {
            downloadListener.onFailure(error)
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val percentage = values[0]!!
        // setting progress percentage
        Log.i(TAG, "Downloading progress : $percentage")
        progressDialog!!.progress = percentage

    }
}