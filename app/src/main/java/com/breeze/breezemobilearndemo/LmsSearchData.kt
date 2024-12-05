package com.breezemobilearndemo

data class LmsSearchData(val searchid: String,val courseName: String, var video_count: Int = 0,var topic_parcentage: Int =0,var topic_sequence: Int =0, var isSelected: Boolean = false )
data class HeaderItem(val headerText: String, val valueItems: List<ValueItem>)
data class ValueItem(val valueHeader: String, val valueText: String, val imageResId: Int)

data class CommentHis(var contentID:String="",var comment:String="",var dateTime:String="")

data class QuestionViewL(var serial: Int=0,var question_id:String="",var question:String="",var optionL:ArrayList<QuestionOptions> = ArrayList())
data class QuestionOptions(var serial:Int=0,var desc:String="",var points:Int=0,var isCorrect:Boolean=false,var isSelected:Boolean=false)

//data class LMSNotiFilterData(var noti_date:String="",var notiL:ArrayList<LMSNotiEntity> = ArrayList())

open class VidBookmark(var topic_id:String="",var topic_name:String="",var content_id:String="",var content_name:String="",var content_desc:String="",
    var content_bitmap:String="",var content_url:String="",var isBookmarked:String="")

data class BookmarkResponse(var user_id:String="",var topic_id:String="",var topic_name:String="",var content_id:String="",var content_name:String="",var content_desc:String="",
                            var content_bitmap:String="",var content_url:String="",var addBookmark:String="")

data class BookmarkFetchResponse(var bookmark_list:ArrayList<VidBookmark> = ArrayList()):BaseResponse()

data class TopicContentWiseAnswerListsFetchResponse(
    var user_id:Int=0,
    var topic_id:Int=0,
    var topic_name:String="",
    var content_id:Int=0,
    var content_name:String="",
    var question_answer_fetch_list: List<Question_answer_fetch_list> = ArrayList()):BaseResponse()
data class Question_answer_fetch_list(var topic_id:Int=0,var content_id:Int=0,var question_id:Int=0,var question:String="",var question_description:String="",var answered:String="",var isCorrectAnswer:Boolean=false,var option_list:ArrayList<Option_list> = ArrayList())
data class Option_list(var question_id:Int=0,var option_id:Int=0,
                       var option_no_1:String="",var option_point_1:Int=0,var isCorrect_1:Boolean=false,
                       var option_no_2:String="",var option_point_2:Int=0,var isCorrect_2:Boolean=false,
                       var option_no_3:String="",var option_point_3:Int=0,var isCorrect_3:Boolean=false,
                       var option_no_4:String="",var option_point_4:Int=0,var isCorrect_4:Boolean=false)

data class Match_Option_list(var option_no_1:String="", var option_no_2:String="", var option_no_3:String="", var option_no_4:String="")

