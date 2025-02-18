package com.example.gymbuddy.pushnotification

import com.example.gymbuddy.friends.FriendRequestInformationDto
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    //metoda to przeslania danych na serwer
    @POST("/send_request") // endpoint do wysyłania wiadomości jednemu userowi
    suspend fun sendFriendRequest(
        @Body message: FriendRequestInformationDto
    )
}

//    @POST("/broadcast") // endpoint do wysyłania wiadomości grupie userow
//    suspend fun broadcast(
//        @Body body: SendMessageDto
//    )