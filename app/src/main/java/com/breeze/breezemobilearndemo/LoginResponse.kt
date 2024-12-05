package com.breezemobilearndemo

import com.breezemobilearndemo.features.login.model.UserCount


class LoginResponse : BaseResponse() {
    var session_token: String? = null
    var user_details: UserDetail? = null
    var user_count: UserCount? = null
    var state_list: ArrayList<LoginStateListDataModel>? = null
}