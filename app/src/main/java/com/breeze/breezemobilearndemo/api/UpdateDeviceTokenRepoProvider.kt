package com.breezemobilearndemo

import com.breezemobilearndemo.api.UpdateDeviceTokenRepo
import com.breezemobilearndemo.api.UpdateDeviceTokenApi

object UpdateDeviceTokenRepoProvider {
    fun updateDeviceTokenRepoProvider(): UpdateDeviceTokenRepo {
        return UpdateDeviceTokenRepo(UpdateDeviceTokenApi.create())
    }
}