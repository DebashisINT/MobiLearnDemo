package com.breezefieldsalesdemo.features.login.api.global_config

import com.breeze.breezemobilearndemo.api.ConfigFetchResponseModel
import com.breezemobilearndemo.NetworkConstant
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST


interface ConfigFetchApi {

    @POST("Configuration/fetch")
    fun getConfigResponse(): Observable<ConfigFetchResponseModel>

    companion object Factory {
        fun create(): ConfigFetchApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(ConfigFetchApi::class.java)
        }
    }
}