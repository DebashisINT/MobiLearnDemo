package com.breezefieldsalesdemo.features.logout.presentation.api

import com.breezemobilearndemo.BaseResponse
import io.reactivex.Observable


class LogoutRepository(val apiService: LogoutApi) {
    fun logout(user_id: String
              ): Observable<BaseResponse> {
        return apiService.getLogoutResponse(user_id)
    }

}