package com.breezemobilearndemo.api

import com.breezemobilearndemo.LoginResponse
import com.breezemobilearndemo.api.LoginApi
import io.reactivex.Observable


class LoginRepository(val apiService: LoginApi) {
    fun login(username: String, password: String,version: String,
              device_token: String): Observable<LoginResponse> {
        return apiService.getLoginResponse(username, password,version, device_token)
    }
}