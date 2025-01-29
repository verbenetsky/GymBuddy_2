package com.example.gymbuddy.data.authentication

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserInformationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userInformationState = MutableStateFlow(UserInformation())
    val userInformationState: StateFlow<UserInformation> = _userInformationState

    init {
        val currentUser = auth.currentUser
    }

    fun updateFirstName(firstName: String) {
        _userInformationState.update { currentState -> currentState.copy(firstName = firstName) }
    }

    fun updateLastName(lastName: String) {
        _userInformationState.update { currentState -> currentState.copy(lastName = lastName) }
    }

    fun updateUsername(username: String) {
        _userInformationState.update { currentState -> currentState.copy(username = username) }
    }

    fun updateDateOfBirth(dateOfBirth: Long) {
        _userInformationState.update { currentState -> currentState.copy(dateOfBirth = dateOfBirth) }
    }

    fun updateGoal(goal: String) {
        _userInformationState.update { currentState -> currentState.copy(goal = goal) }
    }

    fun removeHobby(hobby: String) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = currentState.hobbies - hobby) }
    }

    fun addHobby(hobby: String) {
        _userInformationState.update { currentState -> currentState.copy(hobbies = currentState.hobbies + hobby) }
    }

    fun saveUserToFiretore(userInformation: UserInformation) {
        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(userInformation.userId)
                    .set(userInformation)
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun x(user: HashMap<String, Any>) {
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun y() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }


}


