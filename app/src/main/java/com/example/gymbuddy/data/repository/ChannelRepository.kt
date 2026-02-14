package com.example.gymbuddy.data.repository

import com.example.gymbuddy.data.model.Channel
import com.example.gymbuddy.data.model.UserFoundInformation
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    suspend fun findChannel(friendId: String, currentUserId: String): Result<String> // sluzy zeby znalezc czy miedzy dwoma userami juz zostal utworzomy channel, zwraca id tego channelu
    suspend fun deleteChannel(channelId: String): Result<Boolean>
    suspend fun deleteAllChannels(currentUserId: String): Result<Boolean>
    suspend fun getLastMessage(channelId: String): Result<String>
    //fun observeLastMessage(channelId: String): Flow<String>
    fun observeChannelsForUser(userId: String): Flow<Set<Channel>>

    fun computeFriendsWithoutChannel(
        allFriends: List<UserFoundInformation>,
        existingChannels: Set<Channel>,
        currentUserId: String
    ): Set<UserFoundInformation>

    suspend fun addChannel(channel: Channel): Result<Boolean>
}