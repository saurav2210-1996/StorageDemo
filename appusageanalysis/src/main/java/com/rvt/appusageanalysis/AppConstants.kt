package com.rvt.appusageanalysis


object APIConst {
    const val DEV_BASE_URL = "https://launchpad-api.edisoninteractive.com/device/v1/"
    const val STAGE_BASE_URL = "https://anthem.com/api/"
    const val PROD_BASE_URL = "https://anthem.com/api/"

    const val DEV_HEADER = "AIzaSyBW8z-pN1xXUhACvaKfbayCyRqPuJInH30"
    const val STAGE_HEADER = ""
    const val PROD_HEADER = ""

    const val DEV_ENV = "dev"
    const val STAGE_ENV = "stage"
    const val PROD_ENV = "prod"

    // CHANGE BASE URL
    const val CURRENT_BASE_URL = DEV_ENV

    const val APP_USAGE_ANALYTIC_DATA = "analytic_data"
}
