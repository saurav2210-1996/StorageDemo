package com.rvt.appusageanalysis

import com.google.gson.annotations.SerializedName

data class CommonResponse(
    @SerializedName("message") var message: String? = null,
    @SerializedName("status") var status: Boolean? = null,
)
