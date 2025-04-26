# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# proguard (jego nastepca R8 (audi R8 lol)) odpowiada za obfuskacje czyli:
#– Zmienia nazwy klas, metod i pól na krótkie, bezsensowne ciągi (a, b, c1…), aby utrudnić dekompilację i zrozumienie Twojego kodu przez osoby trzecie.
#– Pozwala to chociaż w niewielkim stopniu chronić logikę przed atakami “reverse-engineering”.
# rowniez za:
# Shrinking (usuwanie nieużywanego kodu) - analizuje kod i wycina z APK te klasy i metody do ktorych ni
# sie nie odwoluje
# rowniez:
# przeprowadza optymalizacje
# wiec tutaj mowily shrinkerowi zeby nie usunal na Credential managera
#
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
*;
}
