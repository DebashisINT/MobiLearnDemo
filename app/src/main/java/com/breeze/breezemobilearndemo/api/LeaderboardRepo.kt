package com.breezemobilearndemo.api

import com.breezemobilearndemo.LeaderboardBranchData
import com.breezemobilearndemo.LeaderboardOverAllData
import com.breezemobilearndemo.LeaderboardOwnData
import com.breezemobilearndemo.api.LeaderboardApi
import io.reactivex.Observable


/**
 * Created by Puja on 10-10-2024.
 */
class LeaderboardRepo(val apiService: LeaderboardApi) {

    fun branchlist(session_token: String): Observable<LeaderboardBranchData> {
        return apiService.branchList(session_token)
    }
    fun ownDatalist(user_id: String,activitybased: String,branchwise: String,flag: String): Observable<LeaderboardOwnData> {
        return apiService.ownDatalist(user_id,activitybased,branchwise,flag)
    }
    fun overAllAPI(user_id: String,activitybased: String,branchwise: String,flag: String): Observable<LeaderboardOverAllData> {
        return apiService.overAllDatalist(user_id,activitybased,branchwise,flag)
    }
}