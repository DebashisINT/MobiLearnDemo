package com.breezemobilearndemo.api

import com.breezemobilearndemo.api.LoginApi
import com.breezemobilearndemo.api.LoginRepository

object LoginRepositoryProvider {
    fun provideLoginRepository(): LoginRepository {
        return LoginRepository(LoginApi.create())
    }
}