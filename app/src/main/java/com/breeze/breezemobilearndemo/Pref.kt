package com.breezemobilearndemo

import com.marcinmoskala.kotlinpreferences.PreferenceHolder


object Pref : PreferenceHolder() {

    var user_id: String? by bindToPreferenceFieldNullable()
    var FirstLogiForTheDayTag: Boolean by bindToPreferenceField(true, "FirstLogiForTheDayTag")
    var user_name: String? by bindToPreferenceFieldNullable()
    var deviceToken: String by bindToPreferenceField("", "deviceToken")
    var temp_user_id: String? by bindToPreferenceFieldNullable()
    var login_time: String? by bindToPreferenceFieldNullable()
    var login_date_time: String? by bindToPreferenceFieldNullable()
    var login_date: String? by bindToPreferenceFieldNullable()
    var isRememberMe: Boolean by bindToPreferenceField(false, "isRememberMe")
    var PhnNo: String by bindToPreferenceField("", "PhnNo")
    var pwd: String by bindToPreferenceField("", "pwd")
    var loginID: String by bindToPreferenceField("", "loginID")
    var logId: String by bindToPreferenceField("", "logId")
    var loginPassword: String by bindToPreferenceField("", "loginPassword")
    //begin mantis id 0027432 loc_k & firebase_k functionality Puja 08-05-2024 v4.2.7
    var loc_k : String by bindToPreferenceField("", "loc_k")
    var firebase_k : String by bindToPreferenceField("", "firebase_k")
    //end mantis id 0027432 loc_k & firebase_k functionality Puja 08-05-2024 v4.2.7
    var IsUserWiseLMSEnable: Boolean by bindToPreferenceField(false, "IsUserWiseLMSEnable")
    var IsUserWiseLMSFeatureOnly: Boolean by bindToPreferenceField(false, "IsUserWiseLMSFeatureOnly")

    var QuestionAfterNoOfContentForLMS: String by bindToPreferenceField("1", "QuestionAfterNoOfContentForLMS")

    var videoCompleteCount: String by bindToPreferenceField("0", "videoCompleteCount")

    var like_count: Int by bindToPreferenceField(0, "like_count")

    var imei: String by bindToPreferenceField("", "imei")

    var share_count: Int by bindToPreferenceField(0, "share_count")

    var session_token: String? by bindToPreferenceFieldNullable()

    var comment_count: Int by bindToPreferenceField(0, "comment_count")

    var correct_answer_count: Int by bindToPreferenceField(0, "correct_answer_count")

    var wrong_answer_count: Int by bindToPreferenceField(0, "wrong_answer_count")

    var content_watch_count: Int by bindToPreferenceField(0, "content_watch_count")

    var LastVideoPlay_TopicID : String by bindToPreferenceField("", "LastVideoPlay_TopicID")
    var LastVideoPlay_TopicName : String by bindToPreferenceField("", "LastVideoPlay_TopicName")
    var LastVideoPlay_ContentID : String by bindToPreferenceField("", "LastVideoPlay_ContentID")
    var LastVideoPlay_ContentName : String by bindToPreferenceField("", "LastVideoPlay_ContentName")
    var LastVideoPlay_VidPosition : String by bindToPreferenceField("", "LastVideoPlay_VidPosition")
    var LastVideoPlay_BitmapURL : String by bindToPreferenceField("", "LastVideoPlay_BitmapURL")
    var LastVideoPlay_ContentDesc : String by bindToPreferenceField("", "LastVideoPlay_ContentDesc")
   // var LastVideoPlay_ContentParcentBar : String by bindToPreferenceField("", "LastVideoPlay_ContentParcentBar")
    var LastVideoPlay_ContentParcent : String by bindToPreferenceField("", "LastVideoPlay_ContentParcent")
   // var LastVideoPlay_ContentParcentStatus : String by bindToPreferenceField("", "LastVideoPlay_ContentParcentStatus")

    var IsAllowGPSTrackingInBackgroundForLMS: Boolean by bindToPreferenceField(true, "IsAllowGPSTrackingInBackgroundForLMS")

    var CurrentBookmarkCount: Int by bindToPreferenceField(0, "CurrentBookmarkCount")

    //Mantis 0027717 Puja 07.10.2024
    var IsVideoAutoPlayInLMS : Boolean by bindToPreferenceField(true, "IsVideoAutoPlayInLMS")

    //Mantis 0027772 Puja 17.10.2024
    var ShowRetryIncorrectQuiz : Boolean by bindToPreferenceField(false, "ShowRetryIncorrectQuiz")

}


