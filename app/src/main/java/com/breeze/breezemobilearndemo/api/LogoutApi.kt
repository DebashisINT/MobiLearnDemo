package com.breezefieldsalesdemo.features.logout.presentation.api

import com.breezemobilearndemo.BaseResponse
import com.breezemobilearndemo.NetworkConstant
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface LogoutApi {
    @FormUrlEncoded
    @POST("Logout/UserLogout")
    fun getLogoutResponse(@Field("user_id") email: String): Observable<BaseResponse>

    /**
     * Companion object to create the GithubApiService
     */
    companion object Factory {
        fun create(): LogoutApi {
            val retrofit = Retrofit.Builder()
                    .client(NetworkConstant.setTimeOut())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(NetworkConstant.BASE_URL)
                    .build()

            return retrofit.create(LogoutApi::class.java)
        }
    }

}