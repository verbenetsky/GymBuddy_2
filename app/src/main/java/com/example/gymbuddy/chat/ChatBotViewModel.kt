package com.example.gymbuddy.chat

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor() : ViewModel() {

    private val _messageListChatBot = MutableStateFlow<List<ChatBotMessage>>(emptyList())
    val messageListChatBot = _messageListChatBot.asStateFlow()

    private val db = Firebase.firestore
    private val currentUserUID = Firebase.auth.currentUser!!.uid

    private val _chatBotResponseState =
        MutableStateFlow<ChatBotResponseState>(ChatBotResponseState.UnActive)
    val chatBotResponseState = _chatBotResponseState.asStateFlow()

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = "REDACTED_GEMINI_KEY" //todo
    )

    fun sendMessage(question: String) {
        viewModelScope.launch {
            val chat = generativeModel.startChat(
                history = _messageListChatBot.value.map { content(it.role) { text(it.message) } }
                    .toList()
            )

            val message = ChatBotMessage(
                message = question,
                role = "user",
                createdAt = System.currentTimeMillis()
            )

            changeChatBotResponseState(ChatBotResponseState.Typing)

            db.collection("chatBot")
                .document(currentUserUID)
                .collection("conversation_with_bot")
                .add(message)

            val fullQuestion = "If the user's query does not pertain to gym workouts, nutrition advice, recovery strategies, " +
                    "or comprehensive fitness planning (including motivation and self-improvement), politely ask for " +
                    "clarification on what specific fitness or nutrition information they are seeking. " +
                    "You are a knowledgeable and experienced gym trainer specializing in workouts, nutrition, recovery, " +
                    "and comprehensive fitness planning. " +
                    "Provide clear, detailed, and practical advice using a friendly and professional tone. " +
                    "Whenever possible, structure your response using bullet points or short numbered steps to enhance clarity. " +
                    "Focus exclusively on topics related to gym, fitness, motivation, self-improvement, and nutrition. " +
                    question


            val response = chat.sendMessage(fullQuestion)

            val responseMessage = ChatBotMessage(
                message = convertMarkdownToHtml(response.text.toString()),
                role = "model",
                createdAt = System.currentTimeMillis()
            )

            db.collection("chatBot")
                .document(currentUserUID)
                .collection("conversation_with_bot")
                .add(responseMessage)

            changeChatBotResponseState(ChatBotResponseState.UnActive)
        }
    }

    fun listenForMessages() {
        db.collection("chatBot")
            .document(currentUserUID)
            .collection("conversation_with_bot")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        _messageListChatBot.value = emptyList()
                    } else {
                        val messages = snapshot.documents.mapNotNull {
                            it.toObject(ChatBotMessage::class.java)
                        }
                        _messageListChatBot.value = messages
                    }
                }
            }
    }

    fun convertMarkdownToHtml(markdownText: String): String {
        // Używamy CommonMark – można też rozszerzyć lub zmienić flavour
        val flavour = CommonMarkFlavourDescriptor()
        // Parsujemy strukturę dokumentu Markdown
        val parser = MarkdownParser(flavour)
        val parsedTree = parser.buildMarkdownTreeFromString(markdownText)
        // Generujemy HTML na podstawie sparsowanego drzewa
        val htmlGenerator = HtmlGenerator(markdownText, parsedTree, flavour)
        return htmlGenerator.generateHtml()
    }


    private fun changeChatBotResponseState(newState: ChatBotResponseState) {
        _chatBotResponseState.value = newState
    }

    fun deleteChatBotConversation(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val querySnapshot = db.collection("chatBot")
                    .document(currentUserUID)
                    .collection("conversation_with_bot")
                    .get()
                    .await()
                val batch = db.batch()
                for (document in querySnapshot) {
                    batch.delete(document.reference)
                }
                batch.commit().await()
                _messageListChatBot.value = emptyList()
                println("All documents have been deleted.")
                onSuccess()
            } catch (e: Exception) {
                println("Error during deletion: $e")
            }
        }

    }
}

sealed class ChatBotResponseState {
    data object Typing : ChatBotResponseState()
    data object UnActive : ChatBotResponseState()
}