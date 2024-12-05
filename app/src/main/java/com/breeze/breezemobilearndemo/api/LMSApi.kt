package com.breezemobilearndemo.api

import com.breezemobilearndemo.BaseResponse
import com.breezemobilearndemo.BookmarkFetchResponse
import com.breezemobilearndemo.BookmarkResponse
import com.breezemobilearndemo.CONTENT_WISE_QA_SAVE
import com.breezemobilearndemo.ContentCountSave_Data
import com.breezemobilearndemo.Crash_Report_Save
import com.breezemobilearndemo.LMSLeaderboardOverAllData
import com.breezemobilearndemo.LMSLeaderboardOwnData
import com.breezemobilearndemo.LMS_CONTENT_INFO
import com.breezemobilearndemo.MyCommentListResponse
import com.breezemobilearndemo.MyLarningListResponse
import com.breezemobilearndemo.NetworkConstant
import com.breezemobilearndemo.SectionsPointsList
import com.breezemobilearndemo.TopicContentWiseAnswerListsFetchResponse
import com.breezemobilearndemo.TopicListResponse
import com.breezemobilearndemo.VideoTopicWiseResponse
import io.reactivex.Observable

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LMSApi {

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicLists")
    fun getTopics(@Field("user_id") user_id: String): Observable<TopicListResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicWiseLists")
    fun getTopicswiseVideoApi(@Field("user_id") user_id: String,@Field("topic_id") topic_id: String): Observable<VideoTopicWiseResponse>

    @POST("LMSInfoDetails/TopicContentDetailsSave")
    fun saveContentInfoApi(@Body lms_content_info: LMS_CONTENT_INFO?): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/LearningContentLists")
    fun getMyLearningContentList(@Field("user_id") user_id: String): Observable<MyLarningListResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/CommentLists")
    fun getCommentInfo(@Field("topic_id") topic_id: String , @Field("content_id") content_id: String): Observable<MyCommentListResponse>

    @POST("LMSInfoDetails/TopicContentWiseQASave")
    fun saveContentWiseQAApi(@Body mCONTENT_WISE_QA_SAVE: CONTENT_WISE_QA_SAVE): Observable<BaseResponse>

    @POST("LMSInfoDetails/ContentCountSave")
    fun saveContentCount(@Body mContentCountSave_Data: ContentCountSave_Data): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSLeaderboardOwnList")
    fun ownDatalist(@Field("user_id") user_id: String,@Field("branchwise") branchwise: String,@Field("flag") flag: String): Observable<LMSLeaderboardOwnData>

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSLeaderboardOverallList")
    fun overAllDatalist(@Field("user_id") user_id: String,@Field("branchwise") branchwise: String,@Field("flag") flag: String): Observable<LMSLeaderboardOverAllData>

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSSectionsPointsList")
    fun overAllDatalist(@Field("session_token") session_token: String): Observable<SectionsPointsList>

    @POST("LMSInfoDetails/LMSSaveBookMark")
    fun bookmarkApiCallApi(@Body obj: BookmarkResponse): Observable<BaseResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSFetchBookMark")
    fun getBookmarkedApiCallApi(@Field("user_id") user_id: String): Observable<BookmarkFetchResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicContentWiseAnswerLists")
    fun getTopicContentWiseAnswerLists(@Field("user_id") user_id: String,@Field("topic_id") topic_id: String,@Field("content_id") content_id: String): Observable<TopicContentWiseAnswerListsFetchResponse>

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicContentWiseAnswerUpdate")
    fun getTopicContentWiseAnswerUpdate(@Field("user_id") user_id: String,@Field("session_token") session_token: String,@Field("topic_id") topic_id: Int,@Field("topic_name") topic_name: String,@Field("content_id") content_id: Int,@Field("question_id") question_id: Int,@Field("question") question: String,@Field("option_id") option_id: Int,
                                        @Field("option_number") option_number: String ,@Field("option_point") option_point: Int, @Field("isCorrect") isCorrect: Boolean): Observable<BaseResponse>


    @POST("LMSInfoDetails/UserWiseAPPCrashDetails")
    fun saveCrashReportToServer(@Body mCrash_Report_Save: Crash_Report_Save): Observable<BaseResponse>

    companion object Factory {
        fun create(): LMSApi {
            val retrofit = Retrofit.Builder()
                .client(NetworkConstant.setTimeOut())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(NetworkConstant.BASE_URL)
                .build()

            return retrofit.create(LMSApi::class.java)
        }
    }
}