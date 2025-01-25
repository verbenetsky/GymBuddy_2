package com.example.gymbuddy.data.authentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.gymbuddy.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

//troche rzeczy jest przestarzala ale wroce do tego troche pozniej zeby naprawic todo

// One Tap Sign-In jest to logowanie przez konto google bezposrednio
class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    // logowanie powinno byc suspend fun
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn( // rozpoczynamy proces logowania
                buildSignInRequest()
            ).await() // await() mozemy wykorzystac tylko w suspend fun
            // await konwertuje callback based operacje na kod wspolbiezny w suspend fun
            // kiedy await jest wywolywane fun zostaje zawieszona zeby na pobocznym watku wykonac operacje
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credentials = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken =
            credentials.googleIdToken // kazdy uzytkownik posiada unikatowy ID, ktory sie stosuje do uwierzytelniena uzytkownika w Firebase
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            //proba zalogowania uzytkwnia w Firebase na podstawie google credentials
            val user = auth.signInWithCredential(googleCredentials).await().user
            //inicjujemy logowanie usera w firebase na podstawie google credentials
            //uzyskujemy obiekt FirebaseUser, ktory zawiera info o zalogowanym uzytkowniku

            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser() : UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    // budujemy zadanie logowania przez google One Tap Sign-In (tworzymy konfiguracje)
    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder().setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true) // apka obsluguje logowanie za pomoca Client Server ID
                .setFilterByAuthorizedAccounts(true) // tutaj to oznacza uzytkowkin bedzie mogl wybrac dowolne konto Google, a nie tylko konta wczesniej uzywane do logowania
                .setServerClientId(context.getString(R.string.default_web_client_id)) // ustawia ID aplikacji na serwerach Googla
                .build()
        )
            .setAutoSelectEnabled(false)// okresla czy logowanie ma byc automatyczne,czyli
            // jesli mamy tylko jedno konto to przy true odrazu nas na niego zaloguje
            // przy false apka zawsze wyswietla ekran wyboru
            .build()
    }
}