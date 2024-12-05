package com.breezemobilearndemo.api

import com.breezemobilearndemo.LoginResponse
import com.breezemobilearndemo.api.LoginApi
import io.reactivex.Observable


class LoginRepository(val apiService: LoginApi) {
    fun login(username: String, password: String, latitude: String, longitude: String, login_time: String, imei: String, version: String, location: String,
              device_token: String): Observable<LoginResponse> {
        return apiService.getLoginResponse(username, password, latitude, longitude, login_time, imei, version, location, device_token)
    }
}