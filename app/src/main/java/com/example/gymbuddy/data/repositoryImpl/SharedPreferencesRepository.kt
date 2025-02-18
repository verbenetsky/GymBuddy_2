package com.example.gymbuddy.data.repositoryImpl

//class SharedPreferencesRepository(context: Context) {
//
//    private val sharedPreferences =
//        context.getSharedPreferences("registration_status", Context.MODE_PRIVATE)
//
//    fun setRegistrationStatus(userId: String, isRegistered: Boolean) {
//        sharedPreferences.edit()
//            .putBoolean(userId, isRegistered)
//            .apply()
//    }
//
//    fun isRegistered(userId: String): Boolean {
//        return sharedPreferences.getBoolean(userId, false) // jesli nic nie ma pod tym kluczem to false
//    }
//
//    fun printSharedPreferences() {
//        // Pobieramy wszystkie wpisy jako mapę klucz-wartość
//        val allEntries: Map<String, *> = sharedPreferences.all
//        // Iterujemy po mapie i wypisujemy każdy wpis
//        for ((key, value) in allEntries) {
//            Log.d("SharedPreferences", "Key: $key, Value: $value")
//        }
//    }
//}