package com.example.gymbuddy.repository

import com.example.gymbuddy.channel.Channel
import com.example.gymbuddy.data.UserFoundInformation
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    suspend fun deleteChannel(channelId: String): Result<Boolean>
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