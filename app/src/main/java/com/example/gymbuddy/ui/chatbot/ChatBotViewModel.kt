package com.example.gymbuddy.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.BuildConfig
import com.example.gymbuddy.data.model.ChatBotMessage
import com.example.gymbuddy.data.repositoryImpl.ChatBotRepositoryImpl
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatBotRepository: ChatBotRepositoryImpl,
) : ViewModel() {

    private val _messageListChatBot = MutableStateFlow<List<ChatBotMessage>>(emptyList())
    val messageListChatBot = _messageListChatBot.asStateFlow()

    private val userId = Firebase.auth.currentUser!!.uid

    private val _chatBotResponseState =
        MutableStateFlow<ChatBotResponseState>(ChatBotResponseState.UnActive)
    val chatBotResponseState = _chatBotResponseState.asStateFlow()

    init {
        viewModelScope.launch {
            chatBotRepository.observeConversation(userId)
                .onEach { msgs ->
                    _messageListChatBot.value = msgs
                    // gdy pojawi się wiadomość od modelu, od razu wyłączamy „typing”
                    if (msgs.lastOrNull()?.role == "model") {
                        _chatBotResponseState.value = ChatBotResponseState.UnActive
                    }
                }
                .launchIn(this)
        }
    }




    fun sendMessage(question: String) {
        viewModelScope.launch {
            _chatBotResponseState.value = ChatBotResponseState.Typing
            val history = _messageListChatBot.value
            val result = chatBotRepository.sendMessage(userId, question, history)
            if (result.isSuccess) {
                println("message sent successfully")
            } else {
                println("error: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    suspend fun deleteConversation(onSuccess: () -> Unit = {}) {
        val res = chatBotRepository.deleteChatBotConversation(userId)
        if (res.isSuccess) {
            _messageListChatBot.value = emptyList()
            onSuccess()
        } else {
            println("chat bot error while deleting conv")
        }
    }
}

sealed class ChatBotResponseState {
    data object Typing : ChatBotResponseState()
    data object UnActive : ChatBotResponseState()
}

// ChatBotModule.kt
@Module
@InstallIn(SingletonComponent::class)
object ChatBotModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel =
        GenerativeModel(
            modelName = BuildConfig.GENERATIVE_MODEL_NAME,
            apiKey = BuildConfig.GENERATIVE_API_KEY
        )
}
