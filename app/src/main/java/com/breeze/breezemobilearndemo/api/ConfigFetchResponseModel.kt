package com.breeze.breezemobilearndemo.api

import com.breezemobilearndemo.BaseResponse


class ConfigFetchResponseModel : BaseResponse() {


    //begin mantis id 0027298 IsShowLeaderBoard functionality Puja 12-03-2024  v4.2.6
    var IsShowLeaderBoard:Boolean? = false
    //end mantis id 0027298 IsShowLeaderBoard functionality Puja 12-03-2024  v4.2.6

    //begin mantis id 0027298 loc_k functionality Puja 08-05-2024  v4.2.7
    var loc_k:String? = ""
    //end mantis id 0027298 loc_k functionality Puja 08-05-2024  v4.2.7

    //begin mantis id 0027298 firebase_k functionality Puja 08-05-2024  v4.2.7
    var firebase_k:String? = ""
    //end mantis id 0027298 firebase_k functionality Puja 08-05-2024  v4.2.7

    //begin mantis id 0027683 QuestionAfterNoOfContentForLMS functionality Puja 05-08-2024  v4.2.9
    var QuestionAfterNoOfContentForLMS:String? = ""
    //end mantis id 0027683 QuestionAfterNoOfContentForLMS functionality Puja 05-08-2024  v4.2.9

    var IsVideoAutoPlayInLMS:Boolean? = true

    var ShowRetryIncorrectQuiz:Boolean? = false

}