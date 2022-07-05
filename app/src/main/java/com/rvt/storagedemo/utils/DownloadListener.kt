package com.rvt.storagedemo.utils

interface DownloadListener {
    fun onSuccess(path: String)
    fun onFailure(error: String)
}