package com.example.gymbuddy.data.authentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.compose.runtime.collectAsState
import com.example.gymbuddy.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Rozpoczyna proces logowania za pomocą Google One Tap.
     * Funkcja jest zawieszona (suspend) i zwraca IntentSender do uruchomienia.
     */

    suspend fun signIn(): IntentSender? {
        return try {
            // Rozpoczynamy proces logowania
            val result = oneTapClient.beginSignIn(buildSignInRequest()).await()
            // await() konwertuje callback-based operacje na kod współbieżny w suspend fun
            // kiedy await jest wywoływane, funkcja zostaje zawieszona, aby na pobocznym wątku wykonać operację
            result.pendingIntent.intentSender
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
    }

    /**
     * Obsługuje wynik logowania z intencji Google One Tap.
     * Funkcja jest zawieszona (suspend) i zwraca wynik logowania w postaci SignInResult.
     */
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            // Pobieramy poświadczenia z intencji
            val credentials = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credentials.googleIdToken
            // Każdy użytkownik posiada unikatowy ID, który się stosuje do uwierzytelnienia użytkownika w Firebase
            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            // Próba zalogowania użytkownika w Firebase na podstawie google credentials
            val user = auth.signInWithCredential(googleCredentials).await().user
            // Inicjujemy logowanie usera w Firebase na podstawie google credentials
            // Uzyskujemy obiekt FirebaseUser, który zawiera informacje o zalogowanym użytkowniku

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        email = email ?: "",
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(data = null, errorMessage = e.message ?: "Unknown error")
        }
    }

    /**
     * Wylogowuje użytkownika z Google One Tap i Firebase.
     */
    suspend fun signOut() {
        try {
            // Wylogowanie z Google One Tap
            oneTapClient.signOut().await()
            // Wylogowanie z Firebase
            auth.signOut()

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            // Możesz dodać dodatkową obsługę błędów tutaj, jeśli jest potrzebna
        }
    }

    /**
     * Zwraca dane aktualnie zalogowanego użytkownika.
     * Jeśli użytkownik nie jest zalogowany, zwraca null.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            email = email ?: "",
            username = displayName,
            profilePictureUrl = photoUrl?.toString(),
        )
    }

    /**
     * Buduje konfigurację logowania przez Google One Tap.
     */
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true) // Aplikacja obsługuje logowanie za pomocą Client Server ID
                    .setFilterByAuthorizedAccounts(true) // Użytkownik będzie mógł wybrać dowolne konto Google, a nie tylko konta wcześniej używane do logowania
                    .setServerClientId(context.getString(R.string.default_web_client_id)) // Ustawia ID aplikacji na serwerach Google
                    .build()
            )
            .setAutoSelectEnabled(false) // Określa, czy logowanie ma być automatyczne
            // Jeśli mamy tylko jedno konto, to przy true od razu nas na niego zaloguje
            // Przy false aplikacja zawsze wyświetla ekran wyboru
            .build()
    }
}
