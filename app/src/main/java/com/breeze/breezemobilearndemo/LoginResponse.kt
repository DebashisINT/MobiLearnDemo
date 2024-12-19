package com.breezemobilearndemo


class LoginResponse : BaseResponse() {
    var session_token: String? = null
    var user_details: UserDetail? = null
}