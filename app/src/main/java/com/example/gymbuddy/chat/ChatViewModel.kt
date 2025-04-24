package com.example.gymbuddy.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbuddy.data.repositoryImpl.ChatRepositoryImpl
import com.example.gymbuddy.pushnotification.FcmApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val fcmApi: FcmApi,
    private val chatRepository: ChatRepositoryImpl
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val message = _messages.asStateFlow()

    private val _currentChatName = MutableStateFlow("")
    val currentChatName = _currentChatName.asStateFlow()

    fun updateCurrentChatName(newName: String) {
        _currentChatName.value = newName
    }

    private var listenJob: Job? = null
    fun startListeningForMessages(channelID: String) {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            chatRepository.listenForMessages(channelID)
                .collect { list ->
                    _messages.value = list
                }
        }
    }

    fun stopListening() {
        // odpinamy listener i kasujemy job
        listenJob?.cancel()
        listenJob = null
    }

    // nie updejtuje sie na biezaco, trzeba przeladowac zeby bylo widac ostatnia wiadomosc todo maybe
    private fun updateLastMessage(message: String, channelID: String) {
        viewModelScope.launch {
            val res = chatRepository.updateLastMessage(channelID, message)
            if (res.isSuccess) {
                println("successfully updated last message")
            } else {
                println("error while updating last message")
            }
        }
    }

    fun findChannel(
        currentUserId: String,
        otherUserId: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val channelId = chatRepository.findChannel(currentUserId, otherUserId)

            // troche callbakowy styl to
            if (!channelId.isNullOrEmpty()) {
                onSuccess(channelId)
            } else {
                println("channel id pusty, funkcja w viewModelu nie zadziala")
                onFailure()
            }
        }
    }

    fun deleteAllImages(channelID: String) {
        viewModelScope.launch {
            val result = chatRepository.deleteAllImages(channelID)
            if (result.isSuccess) {
                println("all images deleted")
            } else {
                val error = result.exceptionOrNull()
                println("Błąd podczas usuwania obrazów: ${error?.message}")
            }
        }
    }

    fun deleteAllMessages(channelID: String) {
        viewModelScope.launch {
            val result = chatRepository.deleteAllMessages(channelID)
            if (result.isSuccess) {
                println("all messages deleted")
            } else {
                val error = result.exceptionOrNull()
                println("Błąd podczas usuwania wiadomosci: ${error?.message}")
            }
        }
    }

    fun sendMessage(channelID: String, message: Message, isSharedWorkout: Boolean = false) {
        viewModelScope.launch {
            val result = chatRepository.sendMessage(channelID, message)

            if (result.isSuccess) {
                println("text successfully message sent")
                sendMessageNotification(message) // wysylamy powiadomienie
                if (isSharedWorkout) {
                    println("isSharedWorkout is true")
                    message.message?.let { updateLastMessage("Workout Summary", channelID) }
                } else {
                    message.message?.let { updateLastMessage(it, channelID) }
                }
            } else {
                val error = result.exceptionOrNull()
                println("Błąd podczas wysylania wiadomosci: ${error?.message}")
            }
        }
    }

    fun sendImageMessage(
        uri: Uri,
        channelId: String,
        senderUsername: String,
        receiverFcmToken: String
    ) {

        viewModelScope.launch {
            val result =
                chatRepository.sendImageMessage(uri, channelId, senderUsername, receiverFcmToken)

            if (result.isSuccess) {
                println("successfully message sent")
                sendMessageNotification(result.getOrNull() ?: Message()) // wysylamy powiadomienie
                updateLastMessage("Photo", channelId)
            } else {
                val error = result.exceptionOrNull()
                println("Błąd podczas wysylania wiadomosci: ${error?.message}")
            }
        }
    }

    private fun sendMessageNotification(message: Message) {
        viewModelScope.launch {
            try {
                fcmApi.sendChatNotification(message)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

//    // todo delete this in the future, two instances in the code
//    private fun convertMarkdownToHtml(markdownText: String): String {
//        // Używamy CommonMark – można też rozszerzyć lub zmienić flavour
//        val flavour = CommonMarkFlavourDescriptor()
//        // Parsujemy strukturę dokumentu Markdown
//        val parser = MarkdownParser(flavour)
//        val parsedTree = parser.buildMarkdownTreeFromString(markdownText)
//        // Generujemy HTML na podstawie sparsowanego drzewa
//        val htmlGenerator = HtmlGenerator(markdownText, parsedTree, flavour)
//        return htmlGenerator.generateHtml()
//    }
}