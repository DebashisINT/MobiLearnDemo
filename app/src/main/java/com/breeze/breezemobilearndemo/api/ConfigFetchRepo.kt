package com.breezefieldsalesdemo.features.login.api.global_config

import com.breeze.breezemobilearndemo.api.ConfigFetchResponseModel
import io.reactivex.Observable


class ConfigFetchRepo(val apiService: ConfigFetchApi) {
    fun configFetch(): Observable<ConfigFetchResponseModel> {
        return apiService.getConfigResponse()
    }
}