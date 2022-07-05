package com.rvt.storagedemo.model

data class CountryModel(
    val `data`: List<Data>,
    val error: Boolean,
    val msg: String
)

data class Data(
    val currency: String,
    val dialCode: String,
    val flag: String,
    val name: String,
    val unicodeFlag: String
)