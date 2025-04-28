package com.example.gymbuddy.data.authentication


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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
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
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(GetPasswordOption())
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


//class CredentialManager(private val activity: Activity) {
//    private val credentialManager = CredentialManager.create(activity)
//
////    private suspend fun tryFetch(req: GetCredentialRequest): SignInResultCred? = try {
//
////    }
//
////        val cred = credentialManager.getCredential(activity, req).credential
////        println(cred.type)
////        when (cred) {
////            is PasswordCredential -> SignInResultCred.Password(cred.id, cred.password)
////            is GoogleIdTokenCredential -> SignInResultCred.Google(cred.idToken)
////
////            is CustomCredential -> when {      // ↙︎ typ federacyjny (Google, Facebook…)
////                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
////                    val gCred = GoogleIdTokenCredential.createFrom(cred.data)
////                    SignInResultCred.Google(gCred.idToken)
////                }
////
////                else -> {
////                    null
////                }
////            }
////
////            else -> null
////        }
////    } catch (e: NoCredentialException) {            // brak danych – naturalna sytuacja
////        null
//
//suspend fun savePassword(email: String, password: String, onSuccess: () -> Unit): SignUpResult =
//    try {
//        val request = CreatePasswordRequest(id = email, password = password)
//        credentialManager.createCredential(
//            activity,
//            request
//        )   // wywołuje bottom-sheet „Zapisz hasło?”
//        onSuccess()
//        SignUpResult.Success(email)
//    } catch (e: CreateCredentialCancellationException) {
//        SignUpResult.Cancelled
//    } catch (e: CreateCredentialException) {
//        SignUpResult.Failure
//    }
//
//private fun buildRequest(id: String, filter: Boolean) =
//    GetCredentialRequest.Builder()
//        .addCredentialOption(GetPasswordOption())
//        .addCredentialOption(
//            GetGoogleIdOption.Builder()
//                .setServerClientId(id)
//                .setFilterByAuthorizedAccounts(filter)
//                .setAutoSelectEnabled(true)    // auto-login przy 1 kredenciale
//                .build()
//        ).build()
//
//    suspend fun silentSignIn(
//        serverClientId: String,
//        interactive: Boolean = false        // gdy true → pełny picker
//    ): SignInResultCred = try {
//        val req = buildRequest(serverClientId, !interactive)
//        val cred = credentialManager.getCredential(activity, req).credential
//        println("type of credentials:")
//        println(cred.type)
//        when (cred) {
//            is PasswordCredential -> SignInResultCred.Password(cred.id, cred.password)
//            is GoogleIdTokenCredential -> SignInResultCred.Google(cred.idToken)
//            is CustomCredential -> when {      // ↙︎ typ federacyjny (Google, Facebook…)
//                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
//                    val gCred = GoogleIdTokenCredential.createFrom(cred.data)
//                    SignInResultCred.Google(gCred.idToken)
//                }
//
//                else -> {
//                    SignInResultCred.NoCredentials
//                }
//            }
//
//            else -> SignInResultCred.NoCredentials
//        }
//    } catch (_: NoCredentialException) {
//        // brak zapisanych danych
//        SignInResultCred.NoCredentials
//    } catch (_: GetCredentialCancellationException) {
//        SignInResultCred.Cancelled
//    } catch (_: Exception) {
//        SignInResultCred.Failure
//    }
//
//    // zapisujemy do credential managera haslo i login zeby potem moc z tego skorzystac
//    suspend fun signUp(email: String, password: String): SignUpResult {
//        return try {
//            credentialManager.createCredential(
//                context = activity,
//                request = CreatePasswordRequest(id = email, password = password)
//            )
//            SignUpResult.Success(email)
//        } catch (e: CreateCredentialCancellationException) {
//            e.printStackTrace()
//            SignUpResult.Cancelled
//        } catch (e: CreateCredentialException) {
//            e.printStackTrace()
//            SignUpResult.Failure
//        }
//    }
//
//
////    suspend fun getCredential(serverClientId: String): SignInResultCred {
////        // 1️⃣  cichy login – tylko wcześniej autoryzowane konta
////        buildRequest(serverClientId, filter = true).run {
////            tryFetch(this) ?:                      // null => brak danych
////            // 2️⃣  pełny picker – pokaż wszystkie konta/hasła
////            tryFetch(buildRequest(serverClientId, filter = false))
////        } ?: return SignInResultCred.NoCredentials
////        return SignInResultCred.Cancelled
////    }
//
//    // tutaj i sie logujemy za pomoca tej metody i rejestrujemy sie
//    suspend fun signInOrSignUp(serverClintId: String): SignInResultCred {
//        return try {
//            val request = GetCredentialRequest.Builder()
//                .addCredentialOption(GetPasswordOption())
//                .addCredentialOption(
//                    GetGoogleIdOption.Builder()
//                        .setServerClientId(serverClintId)
//                        .setFilterByAuthorizedAccounts(true)
//                        .build()
//                )
//                .build()
//
//            val response = credentialManager.getCredential(activity, request)
//            val cred = response.credential
//
//            when (cred) {
//                is PasswordCredential -> {
//                    return SignInResultCred.Password(cred.id, cred.password)
//                }
//
//                is GoogleIdTokenCredential -> {
//                    return SignInResultCred.Google(cred.idToken)
//                }
//
//                else -> {
//                    return SignInResultCred.Failure
//                }
//            }
//        } catch (e: GetCredentialCancellationException) {
//            SignInResultCred.Cancelled
//        } catch (e: Exception) {
//            SignInResultCred.Failure
//        }
//    }
//
////    suspend fun signIn(): SignInResultCred {
////        return try {
////            val credentialResponse = credentialManager.getCredential(
////                context = activity,
////                request = GetCredentialRequest(
////                    credentialOptions = listOf(
////                        GetPasswordOption()
////                    )
////                )
////            )
////            val credentialPas = credentialResponse.credential as? PasswordCredential ?: return SignInResultCred.Failure
////            SignInResultCred.Success(credentialPas.id, credentialPas.password)
////        } catch (e: GetCredentialCancellationException) {
////            e.printStackTrace()
////            SignInResultCred.Cancelled
////        } catch (e: NoCredentialException) {
////            e.printStackTrace()
////            SignInResultCred.NoCredentials
////        } catch (e: GetCredentialException) {
////            e.printStackTrace()
////            SignInResultCred.Failure
////        }
////    }
//}