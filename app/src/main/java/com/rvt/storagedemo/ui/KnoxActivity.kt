package com.rvt.storagedemo.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.rvt.storagedemo.R
import com.rvt.storagedemo.knox.KnoxAdminReceiver
import com.samsung.android.knox.EnterpriseDeviceManager
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager
import kotlinx.android.synthetic.main.activity_knox.*


class KnoxActivity : AppCompatActivity() {

    val knoxKLMLicenseKey = "KLM09-FUSCY-O776Y-HXHNJ-IHTS2-OHN38"
    companion object {
        private val KNOX_APP_MGMT_PERMISSION = "com.samsung.android.knox.permission.KNOX_APP_MGMT"
        private val KNOX_HW_CONTROL_PERMISSION = "com.samsung.android.knox.permission.KNOX_HW_CONTROL"
        private val KNOX_NDA_PERIPHERAL_RT_PERMISSION = "com.samsung.android.knox.permission.KNOX_NDA_PERIPHERAL_RT"

        private val PERMISSIONS = arrayOf(
            KNOX_APP_MGMT_PERMISSION,
            KNOX_HW_CONTROL_PERMISSION,
            KNOX_NDA_PERIPHERAL_RT_PERMISSION
        )
    }

    private var mDeviceAdmin: ComponentName? = null
    private var mDevicePolicyManager: DevicePolicyManager? = null

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knox)

        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mDeviceAdmin = ComponentName(this@KnoxActivity, KnoxAdminReceiver::class.java)

        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val ungrantList = permissions.entries.filter { it.value == false }
            permissions.entries.forEach {
                Log.e("TAG", "${it.key} = ${it.value}")
            }
        }
        checkPermissions()
        setOnClickListener()
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

    private fun setOnClickListener() {

        activateAdminbtn.setOnClickListener {
            activateAdmin()
        }

        deactivateAdminbtn.setOnClickListener {
            deactivateAdmin()
        }

        activateLicencebtn.setOnClickListener {
            activateLicense(knoxKLMLicenseKey)
        }

        deactivateLicenseBtn.setOnClickListener {

        }

        toggleCamerabtn.setOnClickListener {

        }

        activateBackwardsCompatibleKeyBtn.setOnClickListener {

        }

        toggleTimaKeystoreState.setOnClickListener {

        }
    }
    


    private fun activateAdmin() {

        // Ask the user to add a new device administrator to the system
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
        // Start the add device admin activity
        activityResultLauncher.launch(intent)
    }

    private fun deactivateAdmin() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        // Deactivate this application as device administrator
        dpm.removeActiveAdmin(ComponentName(this, KnoxAdminReceiver::class.java))
    }

    var activityResultLauncher = registerForActivityResult(
        StartActivityForResult(),
        ActivityResultCallback { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> Log.e("TAG", "Device admin activation cancelled")
                RESULT_OK -> {
                    Log.e("TAG", "Device administrator activated")
                    // Knox License
                    activateLicense(knoxKLMLicenseKey)
                }
            }
        })

    private fun activateLicense(key: String?) {
        // Instantiate the KnoxEnterpriseLicenseManager class to use the activateLicense method
        val licenseManager = KnoxEnterpriseLicenseManager.getInstance(this)
        try {
            // License Activation
            licenseManager.activateLicense(key)
            Log.e("TAG", "Activating license...")
        } catch (e: Exception) {
            Log.e("TAG", e.localizedMessage)
        }
    }


    private fun toggleCameraState() {
        // Instantiate the EnterpriseDeviceManager class
        val enterpriseDeviceManager = EnterpriseDeviceManager.getInstance(this)
        // Get the RestrictionPolicy class where the setCameraState method lives
        val restrictionPolicy = enterpriseDeviceManager.restrictionPolicy
        val isCameraEnabled = restrictionPolicy.isCameraEnabled(false)
        try {
            // Toggle the camera state on/off
            val result = restrictionPolicy.setCameraState(!isCameraEnabled)
            if (result) {
                Log.e("TAG", "camera_state : ${!isCameraEnabled}")
            } else {
                Log.e("TAG", "camera_state : camera_fail")
            }
        } catch (e: SecurityException) {
            Log.e("TAG", e.localizedMessage)
        }
    }
}