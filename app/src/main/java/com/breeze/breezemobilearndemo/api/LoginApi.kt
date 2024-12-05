package com.breezemobilearndemo.api

import com.breezemobilearndemo.LoginResponse
import com.breezemobilearndemo.NetworkConstant
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface LoginApi {
    @FormUrlEncoded
    @POST("LMSUserLogin/Login")
    fun getLoginResponse(@Field("username") email: String, @Field("password") password: String,
                         @Field("version_name") version: String, @Field("device_token") device_token: String)
            : Observable<LoginResponse>


    companion object Factory {
        fun create(): LoginApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(LoginApi::class.java)
        }

        fun loginImg(): LoginApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.ADD_SHOP_BASE_URL)
                    .build()

            return retrofit.create(LoginApi::class.java)
        }
    }

}