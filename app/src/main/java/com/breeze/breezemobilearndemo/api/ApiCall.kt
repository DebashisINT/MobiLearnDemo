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
import com.breezemobilearndemo.SectionsPointsList
import com.breezemobilearndemo.TopicContentWiseAnswerListsFetchResponse
import com.breezemobilearndemo.TopicListResponse
import com.breezemobilearndemo.VideoTopicWiseResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiCall {

    //api for get topic list
    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicLists")
    suspend fun getTopics(@Field("user_id") user_id:String): TopicListResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicWiseLists")
    suspend fun getTopicsWiseVideo(@Field("user_id") user_id: String,@Field("topic_id") topic_id: String): VideoTopicWiseResponse

    @POST("LMSInfoDetails/TopicContentDetailsSave")
    suspend fun saveContentInfoApi(@Body lms_content_info: LMS_CONTENT_INFO?): BaseResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/LearningContentLists")
    suspend fun getMyLearningContentList(@Field("user_id") user_id: String): MyLarningListResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/CommentLists")
    suspend fun getCommentInfo(@Field("topic_id") topic_id: String , @Field("content_id") content_id: String): MyCommentListResponse

    @POST("LMSInfoDetails/TopicContentWiseQASave")
    suspend fun saveContentWiseQAApi(@Body mCONTENT_WISE_QA_SAVE: CONTENT_WISE_QA_SAVE): BaseResponse

    @POST("LMSInfoDetails/ContentCountSave")
    suspend fun saveContentCount(@Body mContentCountSave_Data: ContentCountSave_Data): BaseResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSLeaderboardOwnList")
    suspend fun ownDatalist(@Field("user_id") user_id: String,@Field("branchwise") branchwise: String,@Field("flag") flag: String): LMSLeaderboardOwnData

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSLeaderboardOverallList")
    suspend fun overAllDatalist(@Field("user_id") user_id: String,@Field("branchwise") branchwise: String,@Field("flag") flag: String): LMSLeaderboardOverAllData

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSSectionsPointsList")
    suspend fun overAllDatalist(@Field("session_token") session_token: String): SectionsPointsList

    @POST("LMSInfoDetails/LMSSaveBookMark")
    suspend fun bookmarkApiCall(@Body obj: BookmarkResponse): BaseResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/LMSFetchBookMark")
    suspend fun getBookmarkedApiCallApi(@Field("user_id") user_id: String): BookmarkFetchResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicContentWiseAnswerLists")
    suspend fun getTopicContentWiseAnswerLists(@Field("user_id") user_id: String,@Field("topic_id") topic_id: String,@Field("content_id") content_id: String): TopicContentWiseAnswerListsFetchResponse

    @FormUrlEncoded
    @POST("LMSInfoDetails/TopicContentWiseAnswerUpdate")
    suspend fun getTopicContentWiseAnswerUpdate(@Field("user_id") user_id: String,@Field("session_token") session_token: String,@Field("topic_id") topic_id: Int,@Field("topic_name") topic_name: String,@Field("content_id") content_id: Int,@Field("question_id") question_id: Int,@Field("question") question: String,@Field("option_id") option_id: Int,
                                        @Field("option_number") option_number: String ,@Field("option_point") option_point: Int, @Field("isCorrect") isCorrect: Boolean): BaseResponse


    @POST("LMSInfoDetails/UserWiseAPPCrashDetails")
    suspend fun saveCrashReportToServer(@Body mCrash_Report_Save: Crash_Report_Save): BaseResponse


/*    companion object {
        fun create(): ApiCall {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY) // Log request and response body
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging) // Add the logging interceptor
                .build()

            val retrofit = Retrofit.Builder()
                .client(client) // Set the custom client
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://3.7.30.86:8072/API/")
                .build()

            return retrofit.create(ApiCall::class.java)
        }

        fun createMultiPart(): ApiCall {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY) // Log request and response body
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging) // Add the logging interceptor
                .build()

            val retrofit = Retrofit.Builder()
                .client(client) // Set the custom client
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://3.7.30.86:8072/")
                .build()

            return retrofit.create(ApiCall::class.java)
        }
    }*/

    companion object{
        fun create():ApiCall{
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://3.7.30.86:8072/API/")
                .build()
            return retrofit.create(ApiCall::class.java)
        }
        fun createMultiPart():ApiCall{
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://3.7.30.86:8072/")
                .build()
            return retrofit.create(ApiCall::class.java)
        }
    }

}