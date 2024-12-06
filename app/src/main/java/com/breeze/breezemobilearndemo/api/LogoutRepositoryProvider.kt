package com.breezefieldsalesdemo.features.logout.presentation.api


object LogoutRepositoryProvider {
    fun provideLogoutRepository(): LogoutRepository {
        return LogoutRepository(LogoutApi.create())
    }
}