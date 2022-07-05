package com.rvt.storagedemo.repository

import com.rvt.storagedemo.model.CountryModel
import okhttp3.ResponseBody
import retrofit2.Call

interface CountryRepository {
    suspend fun getCountryList() : CountryModel
}