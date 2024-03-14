package com.improveskills.habitstracker.Utils

import android.content.Context
import android.graphics.drawable.Drawable

class ExFunctions {



    fun loadImageFromAssets(context: Context, imageName: String): Drawable? {
        val assetManager = context.assets
        return try {
            val inputStream = assetManager.open(imageName)
            Drawable.createFromStream(inputStream, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}