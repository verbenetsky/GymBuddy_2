package com.example.gymbuddy.fcm

import com.example.gymbuddy.data.model.AcceptOrDeclineOrRemoveFriendDto
import com.example.gymbuddy.data.model.FriendRequestDto
import com.example.gymbuddy.data.model.Message
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    //metoda to przeslania danych na serwer
    @POST("/send_request") // endpoint do wysyłania wiadomości jednemu userowi
    suspend fun sendFriendRequest(
        @Body message: FriendRequestDto
    )

    @POST("/send_chat_notification")
    suspend fun sendChatNotification(
        @Body message: Message
    )

    @POST("/send_decline_notification")
    suspend fun sendDeclineNotification(
        @Body declineFriendRequestDto: AcceptOrDeclineOrRemoveFriendDto
    )

    @POST("/send_accept_notification")
    suspend fun sendAcceptNotification(
        @Body acceptFriendRequestDto: AcceptOrDeclineOrRemoveFriendDto
    )

    @POST("/send_remove_notification")
    suspend fun sendRemoveNotification(
        @Body removeDto: AcceptOrDeclineOrRemoveFriendDto
    )
}

