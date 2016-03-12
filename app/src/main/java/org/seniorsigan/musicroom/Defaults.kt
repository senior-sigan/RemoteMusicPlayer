package org.seniorsigan.musicroom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable

class Defaults(private val context: Context) {
    val cover: Drawable
        get() = context.resources.getDrawable(R.drawable.default_album_art_big_card, context.theme)

    val coverBitmap: Bitmap
        get() = BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art_big_card)
}