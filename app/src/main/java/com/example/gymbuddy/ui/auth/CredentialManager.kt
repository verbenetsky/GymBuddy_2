package com.example.gymbuddy.ui.auth


import android.app.Activity
import android.util.Base64
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.gymbuddy.data.model.SignInResultCred
import com.example.gymbuddy.data.model.SignUpResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom


class CredentialManager(private val activity: Activity) {
    private val credentialManager = CredentialManager.create(activity)
    private val serverClientId =
        "359448700030-e6qcjkoqntc9vnics2vhse2u3nd3dtaa.apps.googleusercontent.com"

    suspend fun signUp(email: String, password: String): SignUpResult {
        return try {
            credentialManager.createCredential(
                context = activity,
                request = CreatePasswordRequest(id = email, password = password)
            )
            SignUpResult.Success(email)
        } catch (e: CreateCredentialCancellationException) {
            e.printStackTrace()
            SignUpResult.Cancelled
        } catch (e: CreateCredentialException) {
            e.printStackTrace()
            SignUpResult.Failure
        }
    }
    
    // funkcja do pobierania zapisanych credentials
    suspend fun signIn(): SignInResultCred {
        return try {
            val nonce = generateNonce()

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(GetPasswordOption(isAutoSelectAllowed = false))
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val cred = response.credential

            when (cred) {
                is PasswordCredential -> {
                    SignInResultCred.Password(cred.id, cred.password)
                }

                is CustomCredential -> {
                    if (cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            //tworzymy obiekt z surowych danych
                            //credential.data to JSON zwrócony przez Credential Manager,
                            // zawierający m.in. pole idToken (czyli JWT od Google).
                            // Metoda createFrom parsuje ten JSON i daje Ci obiekt, z
                            // którego łatwo wyciągniesz sam JWT:
                            // val idTokenString = googleIdTokenCredential.idToken
                            // ale ogolnie to trzeba jescze zweryfikowac ten token na serwerze
                            // zeby sprawdzic czy napewno to google wystawilo ten token

                            val googleIdTokenCredential =
                                GoogleIdTokenCredential.createFrom(cred.data)

                            val idTokenString = googleIdTokenCredential.idToken
                            //weryfikujemy
                            val payload = verifyGoogleIdToken(idTokenString, serverClientId)

                            if (payload == null) {
                                println("token nie zostal zweryfikowany")
                                SignInResultCred.Failure
                            } else {
                                payload.let {
                                    val uid = it.subject
                                    val email = it.email
                                    SignInResultCred.Google(idTokenString, email, uid)
                                }
                            }

                        } catch (e: GoogleIdTokenParsingException) {
                            println("Received an invalid google id token response")
                            SignInResultCred.Failure
                        }
                    } else {
                        println("Unexpected type of credential")
                        SignInResultCred.Failure
                    }
                }
                else -> {
                    SignInResultCred.NoCredentials
                }
            }

        } catch (e: GetCredentialCancellationException) {
            e.printStackTrace()
            SignInResultCred.Cancelled
        } catch (e: NoCredentialException) {
            e.printStackTrace()
            SignInResultCred.NoCredentials
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            SignInResultCred.Failure
        }
    }

    private suspend fun verifyGoogleIdToken(
        idTokenString: String,
        clientId: String
    ): GoogleIdToken.Payload? = withContext(Dispatchers.IO) {

        val verifier = GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(listOf(clientId))
            .build()
        verifier.verify(idTokenString)?.payload
    }


    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}


