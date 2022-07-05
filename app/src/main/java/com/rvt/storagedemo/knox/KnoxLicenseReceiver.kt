package com.rvt.storagedemo.knox

import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.samsung.android.knox.license.EnterpriseLicenseManager
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager


class KnoxLicenseReceiver : BroadcastReceiver() {

    private val DEFAULT_ERROR_CODE = -1

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            // No intent action is available
            showToast(context!!, "no_intent")
        } else {
            val action: String = intent.action!!
            if (action == KnoxEnterpriseLicenseManager.ACTION_LICENSE_STATUS) {
                // ELM activation result Intent is obtained
                val errorCode: Int = intent.getIntExtra(KnoxEnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE, DEFAULT_ERROR_CODE)
                if (errorCode == KnoxEnterpriseLicenseManager.ERROR_NONE) {
                    // ELM activated successfully
                    showToast(context!!, "kpe_activated_succesfully")
                    Log.d("SampleLicenseReceiver", "kpe_activated_succesfully")
                } else {
                    // KPE activation failed
                    // Display KPE error message
                    val errorMessage: String = getKPEErrorMessage(context!!, intent, errorCode)
                    showToast(context, errorMessage)
                    Log.d("SampleLicenseReceiver", errorMessage)
                }
            } else if (action == EnterpriseLicenseManager.ACTION_LICENSE_STATUS) {
                // Backwards-compatible key activation result Intent is obtained
                val errorCode: Int = intent.getIntExtra(
                    EnterpriseLicenseManager.EXTRA_LICENSE_ERROR_CODE, DEFAULT_ERROR_CODE
                )
                if (errorCode == EnterpriseLicenseManager.ERROR_NONE) {
                    // Backwards-compatible key activated successfully
                    showToast(context!!, "elm_action_successful")
                    Log.d("SampleLicenseReceiver", "elm_action_successful")
                } else {
                    // Backwards-compatible key activation failed
                    // Display backwards-compatible key error message
                    val errorMessage: String = getELMErrorMessage(context!!, intent, errorCode)
                    showToast(context, errorMessage)
                    Log.d("SampleLicenseReceiver", errorMessage)
                }
            }
        }
    }


    private fun getELMErrorMessage(context: Context, intent: Intent, errorCode: Int): String {
        return when (errorCode) {
                EnterpriseLicenseManager.ERROR_INTERNAL -> "err_elm_internal"
                EnterpriseLicenseManager.ERROR_INTERNAL_SERVER -> "err_elm_internal_server"
                EnterpriseLicenseManager.ERROR_INVALID_LICENSE -> "err_elm_licence_invalid_license"
                EnterpriseLicenseManager.ERROR_INVALID_PACKAGE_NAME -> "err_elm_invalid_package_name"
                EnterpriseLicenseManager.ERROR_LICENSE_TERMINATED -> "err_elm_licence_terminated"
                EnterpriseLicenseManager.ERROR_NETWORK_DISCONNECTED -> "err_elm_network_disconnected"
                EnterpriseLicenseManager.ERROR_NETWORK_GENERAL -> "err_elm_network_general"
                EnterpriseLicenseManager.ERROR_NOT_CURRENT_DATE -> "err_elm_not_current_date"
                EnterpriseLicenseManager.ERROR_NULL_PARAMS -> "err_elm_null_params"
                EnterpriseLicenseManager.ERROR_SIGNATURE_MISMATCH -> "err_elm_sig_mismatch"
                EnterpriseLicenseManager.ERROR_UNKNOWN -> "err_elm_unknown"
                EnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT -> "err_elm_user_disagrees_license_agreement"
                EnterpriseLicenseManager.ERROR_VERSION_CODE_MISMATCH -> "err_elm_ver_code_mismatch"
                else -> {
                    // Unknown error code
                    val errorStatus = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS)
                    "err_elm_code_unknown$errorCode : $errorStatus"
                }
            }
    }

    private fun getKPEErrorMessage(context: Context, intent: Intent, errorCode: Int): String {
        return when (errorCode) {
                KnoxEnterpriseLicenseManager.ERROR_INTERNAL -> "err_kpe_internal"
                KnoxEnterpriseLicenseManager.ERROR_INTERNAL_SERVER -> "err_kpe_internal_server"
                KnoxEnterpriseLicenseManager.ERROR_INVALID_LICENSE -> "err_kpe_licence_invalid_license"
                KnoxEnterpriseLicenseManager.ERROR_INVALID_PACKAGE_NAME -> "err_kpe_invalid_package_name"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_TERMINATED -> "err_kpe_licence_terminated"
                KnoxEnterpriseLicenseManager.ERROR_NETWORK_DISCONNECTED -> "err_kpe_network_disconnected"
                KnoxEnterpriseLicenseManager.ERROR_NETWORK_GENERAL -> "err_kpe_network_general"
                KnoxEnterpriseLicenseManager.ERROR_NOT_CURRENT_DATE -> "err_kpe_not_current_date"
                KnoxEnterpriseLicenseManager.ERROR_NULL_PARAMS -> "err_kpe_null_params"
                KnoxEnterpriseLicenseManager.ERROR_UNKNOWN -> "err_kpe_unknown"
                KnoxEnterpriseLicenseManager.ERROR_USER_DISAGREES_LICENSE_AGREEMENT -> "err_kpe_user_disagrees_license_agreement"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_DEACTIVATED -> "err_kpe_license_deactivated"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_EXPIRED -> "err_kpe_license_expired"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_QUANTITY_EXHAUSTED -> "err_kpe_license_quantity_exhausted"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_ACTIVATION_NOT_FOUND -> "err_kpe_license_activation_not_found"
                KnoxEnterpriseLicenseManager.ERROR_LICENSE_QUANTITY_EXHAUSTED_ON_AUTO_RELEASE -> "err_kpe_license_quantity_exhausted_on_auto_release"
                else -> {
                    // Unknown error code
                    val errorStatus = intent.getStringExtra(KnoxEnterpriseLicenseManager.EXTRA_LICENSE_STATUS)
                    "err_kpe_code_unknown  $errorCode : $errorStatus"
                }
            }
    }
}