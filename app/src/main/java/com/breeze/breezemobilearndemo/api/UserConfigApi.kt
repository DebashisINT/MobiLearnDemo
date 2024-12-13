package com.breezefieldsalesdemo.features.login.api.user_config

import com.breezefieldsalesdemo.features.login.model.userconfig.UserConfigResponseModel
import com.breezemobilearndemo.NetworkConstant
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface UserConfigApi {
    @FormUrlEncoded
    @POST("Configuration/Userwise")
    fun getUserConfigResponse(@Field("user_id") user_id: String): Observable<UserConfigResponseModel>


    companion object Factory {
        fun create(): UserConfigApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOutNoRetry())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(UserConfigApi::class.java)
        }
    }
}