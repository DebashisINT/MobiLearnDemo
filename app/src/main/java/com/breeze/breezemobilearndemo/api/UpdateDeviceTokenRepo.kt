package com.breezemobilearndemo.api

import com.breezemobilearndemo.BaseResponse
import com.breezemobilearndemo.Pref
import io.reactivex.Observable


class UpdateDeviceTokenRepo(val apiService: UpdateDeviceTokenApi) {
    fun updateDeviceToken(deviceToken: String): Observable<BaseResponse> {
        return apiService.updateDeviceToken(Pref.user_id!!, Pref.session_token!!, deviceToken, "Android")
    }

}