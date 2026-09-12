package com.example.entregable1dam.util

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.entregable1dam.R

object ImageResolver {
    @DrawableRes
    fun resolve(context: Context, imageName: String): Int {
        val id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.photo_placeholder
    }
}
