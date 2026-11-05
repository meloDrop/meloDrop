package com.app.melodrop.data

import com.app.melodrop.network.RetrofitClient
import com.app.melodrop.network.dto.FollowUserOut

// Talks to the backend routes of the follows
class FollowsRepository {

    private val api = RetrofitClient.api

    // "Suivre" button: the drops of this user can come first in Discover
    suspend fun follow(userId: Int) = api.followUser(userId)

    // "Abonné" button tapped again: stops following
    suspend fun unfollow(userId: Int) = api.unfollowUser(userId)

    // The users who follow this user
    suspend fun getFollowers(userId: Int): List<FollowUserOut> = api.getFollowers(userId)

    // The users this user follows
    suspend fun getFollowing(userId: Int): List<FollowUserOut> = api.getFollowing(userId)
}
