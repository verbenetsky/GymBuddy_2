package com.example.gymbuddy.pushnotification

import com.example.gymbuddy.chat.Message
import com.example.gymbuddy.friends.FriendRequestInformationDto
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    //metoda to przeslania danych na serwer
    @POST("/send_request") // endpoint do wysyłania wiadomości jednemu userowi
    suspend fun sendFriendRequest(
        @Body message: FriendRequestInformationDto
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

//    @POST("/send_reminder_norification")
//    suspend fun sendReminderNotification(
//        @Body reminderWorkoutDto: ReminderWorkoutDto
//    )
}

