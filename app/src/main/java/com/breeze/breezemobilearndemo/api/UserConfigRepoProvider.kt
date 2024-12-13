package com.breezefieldsalesdemo.features.login.api.user_config

object UserConfigRepoProvider {
    fun provideUserConfigRepository(): UserConfigRepo {
        return UserConfigRepo(UserConfigApi.create())
    }
}