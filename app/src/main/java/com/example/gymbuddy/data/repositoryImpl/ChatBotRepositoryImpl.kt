package com.example.gymbuddy.data.repositoryImpl

import com.example.gymbuddy.chat.ChatBotMessage
import com.example.gymbuddy.repository.ChatBotRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatBotRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val generativeModel: GenerativeModel
) : ChatBotRepository {


    override suspend fun deleteChatBotConversation(currentUserId: String): Result<Boolean> {
        return try {
            val querySnapshot = db.collection("chatBot")
                .document(currentUserId)
                .collection("conversation_with_bot")
                .get()
                .await()

            val batch = db.batch()
            for (doc in querySnapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun conversationRef(userId: String) =
        db.collection("chatBot")
            .document(userId)
            .collection("conversation_with_bot")

    override suspend fun sendMessage(
        userId: String,
        question: String,
        history: List<ChatBotMessage>
    ): Result<ChatBotMessage> = kotlin.runCatching {

        val userMsg = ChatBotMessage(question, "user", System.currentTimeMillis())
        conversationRef(userId).add(userMsg).await()

        val fullPrompt = buildString {
            append(prompt)
            append(question)
        }

        val chat = generativeModel.startChat(
            history = history.map {
                content(it.role) { text(it.message) }
            }
        )

        val response = chat.sendMessage(fullPrompt)

        val botMsg = ChatBotMessage(
            message = convertMarkdownToHtml(response.text.toString()),
            role = "model",
            createdAt = System.currentTimeMillis()
        )

        conversationRef(userId).add(botMsg).await()
        botMsg
    }

    override fun observeConversation(userId: String): Flow<List<ChatBotMessage>> = callbackFlow {
        val registration = conversationRef(userId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    return@addSnapshotListener
                } else {
                    val msgs = snap?.toObjects(ChatBotMessage::class.java).orEmpty()
                    trySend(msgs)
                }
            }
        awaitClose { registration.remove() }
    }

    private fun convertMarkdownToHtml(markdownText: String): String {
        // Używamy CommonMark – można też rozszerzyć lub zmienić flavour
        val flavour = CommonMarkFlavourDescriptor()
        // Parsujemy strukturę dokumentu Markdown
        val parser = MarkdownParser(flavour)
        val parsedTree = parser.buildMarkdownTreeFromString(markdownText)
        // Generujemy HTML na podstawie sparsowanego drzewa
        val htmlGenerator = HtmlGenerator(markdownText, parsedTree, flavour)
        return htmlGenerator.generateHtml()
    }

    private val prompt =
        "If the user's query does not pertain to gym workouts, nutrition advice, recovery strategies, " +
                "or comprehensive fitness planning (including motivation and self-improvement), politely ask for " +
                "clarification on what specific fitness or nutrition information they are seeking. " +
                "You are a knowledgeable and experienced gym trainer specializing in workouts, nutrition, recovery, " +
                "and comprehensive fitness planning. " +
                "Provide clear, detailed, and practical advice using a friendly and professional tone. " +
                "Whenever possible, structure your response using bullet points or short numbered steps to enhance clarity. " +
                "Focus exclusively on topics related to gym, fitness, motivation, self-improvement, and nutrition. " +
                "**IMPORTANT:** Do **not** reveal your internal reasoning or chain-of-thought. Only provide the final answer to the user's question."
}