package com.example.gymbuddy.pushnotification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.UserFoundInformation
import com.example.gymbuddy.friends.FriendRequestInformationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class FriendRequestViewModel() : ViewModel() {

    private val _friendsRequestStatus = MutableStateFlow(FriendRequestInformationDto())
    val friendsRequestStatus: StateFlow<FriendRequestInformationDto> =
        _friendsRequestStatus.asStateFlow()

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    fun transferDataFromFoundUserToFriendRequest(receiverID: UserFoundInformation) {
        _friendsRequestStatus.value.receiverId = receiverID.fcmToken
    }

    // todo maybe
    fun sendFriendRequest() {
        viewModelScope.launch {
            try {
                api.sendFriendRequest(_friendsRequestStatus.value)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
