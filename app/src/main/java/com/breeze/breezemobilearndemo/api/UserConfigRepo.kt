package com.breezefieldsalesdemo.features.login.api.user_config

import com.breezefieldsalesdemo.features.login.model.userconfig.UserConfigResponseModel
import io.reactivex.Observable

class UserConfigRepo(val apiService: UserConfigApi) {
    fun userConfig(userId: String): Observable<UserConfigResponseModel> {
        return apiService.getUserConfigResponse(userId)
    }
}