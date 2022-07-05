package com.rvt.knox

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.rvt.knox.receiver.KnoxAdminReceiver
import com.rvt.knox.receiver.KnoxLicenseReceiver
import com.samsung.android.knox.AppIdentity
import com.samsung.android.knox.EnterpriseDeviceManager
import com.samsung.android.knox.application.ApplicationPolicy
import com.samsung.android.knox.license.EnterpriseLicenseManager
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager

class KnoxActivationManager {


    val TAG = KnoxActivationManager::class.simpleName

    private var activationCallback: ActivationCallback? = null
    private var mDeviceAdmin: ComponentName? = null

    private val knoxLicenseReceiver: KnoxLicenseReceiver = object : KnoxLicenseReceiver() {
        override fun onKnoxLicenseActivated(context: Context?) {
            if (activationCallback != null) {
                activationCallback?.onKnoxLicenseActivated()
            }
        }

        override fun onBackwardLicenseActivated(context: Context?) {
            if (activationCallback != null) {
                activationCallback?.onBackwardLicenseActivated()
            }
        }

        override fun onLicenseActivationFailed(context: Context?, errorMessage: String,errorCode: Int) {
            if (activationCallback != null) {
                activationCallback?.onLicenseActivateFailed(errorCode, errorMessage)
            }
        }
    }

    fun register(context: Context, callback: ActivationCallback) {
        this.activationCallback = callback
        val intentFilter = IntentFilter()
        intentFilter.addAction(EnterpriseLicenseManager.ACTION_LICENSE_STATUS)
        intentFilter.addAction(KnoxLicenseReceiver.ACTION_ELM_LICENSE_STATUS)
        intentFilter.addAction(KnoxLicenseReceiver.ACTION_KLM_LICENSE_STATUS)
        context.registerReceiver(knoxLicenseReceiver, intentFilter)
    }

    private fun activateAdmin(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        mDeviceAdmin = ComponentName(activity, KnoxAdminReceiver::class.java)

        // Ask the user to add a new device administrator to the system
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
        // Start the add device admin activity
        launcher.launch(intent)
    }

    private fun deactivateAdmin(activity: Activity) {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        // Deactivate this application as device administrator
        dpm.removeActiveAdmin(ComponentName(activity, KnoxAdminReceiver::class.java))
    }

    fun activateLicense(context: Context, key: String) {
        // Instantiate the KnoxEnterpriseLicenseManager class to use the activateLicense method
        val licenseManager = KnoxEnterpriseLicenseManager.getInstance(context)
        try {
            // License Activation
            licenseManager.activateLicense(key)
            Log.e("TAG", "Activating license...")
        } catch (e: Exception) {
            Log.e("TAG", e.localizedMessage)
        }
    }

    fun downloadAPK(context: Context,listener: ProgressListener) {
        val absPathToAPKFile =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + "anthem-app-download.apk"
        val downloadId: Int =
            PRDownloader.download("url", absPathToAPKFile, "anthem-app-download.apk")
                .build()
                .setOnStartOrResumeListener { }
                .setOnPauseListener {}
                .setOnCancelListener {}
                .setOnProgressListener {

                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        installUploadedApp(context, absPathToAPKFile, listener)
                    }

                    override fun onError(error: com.downloader.Error?) {
                        TODO("Not yet implemented")
                    }
                })
    }

    private fun installUploadedApp(context: Context, pathToApp: String, listener: ProgressListener) {
        val edm = EnterpriseDeviceManager.getInstance(context)
        val appPolicy = edm.applicationPolicy
        try {
            val result = appPolicy.updateApplication(pathToApp)
            if (result) {
                listener.progressUpdated(
                    "Installing an application package has been successful!",
                    false
                )
                listener.progressComplete(true)
            } else {
                listener.progressUpdated("Installing an application package has failed.", true)
            }
        } catch (e: SecurityException) {
            listener.progressUpdated("SecurityException: " + e.localizedMessage, true)
        }
    }

    fun allowWifi(context: Context,enable: Boolean) {
        val edm = EnterpriseDeviceManager.getInstance(context)
        val restrictionPolicy = edm.restrictionPolicy
        // enable/disable wi-fi UI control
        // enable/disable wi-fi UI control
        try {
            val result = restrictionPolicy.allowWiFi(enable)
            if (result) {
                Log.d(TAG, "allowWifi has been successful!")
            } else {
                Log.d(TAG, "allowWifi has failed.")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException: " + e.localizedMessage)
        }
    }

    fun grantAllRuntimePermissions(context: Context,packageName: String) {
        try {
            // give all runtime permissions using ApplicationPolicy
            val appIdentity = AppIdentity(packageName, null)
            val edm = EnterpriseDeviceManager.getInstance(context)
            val appPolicy = edm.applicationPolicy
            appPolicy.addAppPackageNameToWhiteList(packageName)
            val ret = appPolicy.applyRuntimePermissions(
                appIdentity,
                null,
                ApplicationPolicy.PERMISSION_POLICY_STATE_GRANT
            )
            if (ret == ApplicationPolicy.ERROR_NONE) {
                Log.d(TAG, "Granted runtime permissions  to package $packageName successfully")
            } else {
                Log.d(TAG, "Failed to grant runtime permissions $packageName to package ")
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "SecurityException:" + e.localizedMessage)
        }
    }
}

interface ProgressListener {
    fun progressUpdated(progress: String, failure: Boolean)
    fun progressComplete(success: Boolean)
}