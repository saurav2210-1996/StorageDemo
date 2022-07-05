package com.rvt.storagedemo.repository

import com.google.gson.Gson
import com.rvt.storagedemo.model.CountryModel
import com.rvt.storagedemo.services.CountryService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.awaitResponse

class CountryRepositoryImp(
    private val countryService: CountryService
) : CountryRepository {
    override suspend fun getCountryList(): CountryModel {
        val data = countryService.getCountryList()
        return Gson().fromJson(data.awaitResponse().body()?.string() ?: "",CountryModel::class.java)
    }
}