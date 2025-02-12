package com.example.gymbuddy.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.gymbuddy.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


object CommonUtils {
    @Composable
    fun logoTheme (): Int {
        val isDarkTheme = isSystemInDarkTheme()
        return if (isDarkTheme) {
            R.drawable.logo_gymbuddy_black_theme
        } else {
            R.drawable.logo_gymbuddy_white_theme
        }
    }

//    fun compressImage(
//        context: Context,
//        imageUri: Uri,
//        maxWidth: Int = 1024,
//        maxHeight: Int = 1024,
//        quality: Int = 80
//    ): ByteArray? {
//        val resolver = context.contentResolver
//
//        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
//        resolver.openInputStream(imageUri)?.use { inputStream ->
//            BitmapFactory.decodeStream(inputStream, null, options)
//        }
//
//        var scale = 1
//        if (options.outWidth > maxWidth || options.outHeight > maxHeight) {
//            scale = Math.max(options.outWidth / maxWidth, options.outHeight / maxHeight)
//        }
//
//        val scaledOptions = BitmapFactory.Options().apply { inSampleSize = scale }
//        val bitmap: Bitmap? = resolver.openInputStream(imageUri)?.use { inputStream ->
//            BitmapFactory.decodeStream(inputStream, null, scaledOptions)
//        }
//
//        return bitmap?.let {
//            ByteArrayOutputStream().apply {
//                it.compress(Bitmap.CompressFormat.JPEG, quality, this)
//            }.toByteArray()
//


}