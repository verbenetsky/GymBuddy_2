plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.gymbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gymbuddy"
        minSdk = 28
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    // AndroidX Credentials – biblioteka ułatwiająca obsługę poświadczeń (np. logowanie)
    implementation(libs.androidx.credentials)
    // Integracja AndroidX Credentials z Play Services (autoryzacja)
    implementation(libs.androidx.credentials.play.services.auth)

    // Firebase Firestore – umożliwia korzystanie z bazy danych Firestore
    implementation(libs.google.firebase.firestore)

    // AndroidX Material Icons Extended – rozszerzony zestaw ikon Material (wersja v15.1)
    implementation(libs.androidx.material.icons.extended.v151)

    // Coil dla Jetpack Compose – biblioteka do ładowania obrazów w Compose
    implementation(libs.coil.compose)
    // Integracja Coil z OkHttp (obsługa sieciowa)
    implementation(libs.coil.network.okhttp)

    // Firebase Auth – obsługa autentykacji użytkowników
    implementation(libs.firebase.auth)
    // Firebase BoM – umożliwia zarządzanie wersjami bibliotek Firebase w jednym miejscu
    implementation(platform(libs.firebase.bom))
    // Ponowne dodanie Firestore (dla synchronizacji wersji przez BoM)
    implementation(libs.firebase.firestore)
    // Google Services – integracja z usługami Google
    implementation(libs.google.services)
    // Play Services Auth – wsparcie dla autoryzacji przez Google Play Services
    implementation(libs.play.services.auth)

    // Accompanist System UI Controller – pomocnicza biblioteka do zarządzania wyglądem systemowego UI (np. status bar)
    implementation(libs.accompanist.systemuicontroller)

    // AndroidX Compose – zestaw bibliotek Compose:
    // Użycie Compose BoM dla zarządzania wersjami
    implementation(platform(libs.androidx.compose.bom))
    // AndroidX Core KTX – rozszerzenia Kotlin dla Androida
    implementation(libs.androidx.core.ktx)
    // Lifecycle Runtime KTX – obsługa cyklu życia komponentów Androida w Kotlinie
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Activity Compose – integracja Compose z Activity
    implementation(libs.androidx.activity.compose)
    // Kolejny raz Compose BoM (jeśli potrzebne, aby zapewnić spójność wersji)
    implementation(platform(libs.androidx.compose.bom))
    // AndroidX UI – podstawowe komponenty UI Compose
    implementation(libs.androidx.ui)

    // Firebase Storage – przechowywanie plików w Firebase Storage
    implementation(libs.firebase.storage)

    // AndroidX Material – standardowe komponenty Material Design
    implementation(libs.androidx.material)

    // AndroidX UI Graphics – obsługa grafiki w Compose
    implementation(libs.androidx.ui.graphics)
    // AndroidX UI Tooling Preview – narzędzia do podglądu w Compose
    implementation(libs.androidx.ui.tooling.preview)
    // AndroidX Material3 – nowe komponenty Material Design 3
    implementation(libs.androidx.material3)
    // AndroidX Navigation Compose – nawigacja w aplikacjach Compose
    implementation(libs.androidx.navigation.compose)

    // AndroidX Espresso Core – biblioteka do testowania UI (instrumentalne testy)
    implementation(libs.androidx.espresso.core)

    // Testowanie:
    // JUnit – biblioteka do testów jednostkowych
    testImplementation(libs.junit)
    // AndroidX JUnit – rozszerzenia JUnit dla Androida
    androidTestImplementation(libs.androidx.junit)
    // Espresso – dodatkowe testy UI
    androidTestImplementation(libs.androidx.espresso.core)
    // Testy Compose – zarządzanie testami UI dla Compose
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Narzędzia debugowania UI Compose (podgląd, manifest)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase Messaging – obsługa powiadomień push (FCM)
    implementation(libs.firebase.messaging)

    // Retrofit – biblioteka do komunikacji HTTP (wywoływanie REST API)
    implementation(libs.retrofit)

    // Moshi Converter – konwerter JSON do obiektów Kotlin/Java dla Retrofit
    implementation(libs.moshi)
    implementation(libs.converter.moshi)

    implementation (libs.androidx.hilt.navigation.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.material3)



}
