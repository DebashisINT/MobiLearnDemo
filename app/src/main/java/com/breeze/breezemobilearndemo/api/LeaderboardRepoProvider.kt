package com.breezemobilearndemo.api

import com.breezemobilearndemo.api.LeaderboardRepo


object LeaderboardRepoProvider {
    fun provideLeaderboardbranchRepository(): LeaderboardRepo {
        return LeaderboardRepo(LeaderboardApi.createWithoutMultipart())
    }
}