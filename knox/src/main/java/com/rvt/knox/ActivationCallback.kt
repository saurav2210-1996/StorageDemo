package com.rvt.knox

interface ActivationCallback {
    fun onDeviceAdminActivated()

    fun onDeviceAdminActivationCancelled()

    fun onKnoxLicenseActivated()

    fun onBackwardLicenseActivated()

    fun onLicenseActivateFailed(errorCode: Int, errorMessage: String?)
}