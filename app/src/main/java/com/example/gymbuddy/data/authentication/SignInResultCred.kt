package com.example.gymbuddy.data.authentication

sealed interface SignInResultCred {

        data class Password(val email: String, val password: String): SignInResultCred
        data class Google(val idToken: String): SignInResultCred
        //data class Success(val email: String, val password: String): SignInResultCred
        data object Cancelled: SignInResultCred
        data object Failure: SignInResultCred
        data object NoCredentials: SignInResultCred
}