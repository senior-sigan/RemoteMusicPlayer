package org.seniorsigan.musicroom

import android.graphics.Bitmap
import java.io.Serializable

class AudioPlayedMessage(): Serializable

data class CommonResponse(
        val success: Boolean,
        val error: String?,
        val data: Any?
): Serializable

data class Track(
        val url: String,
        val title: String,
        val artist: String,
        val cover: Bitmap
): Serializable {
    val name: String
        get() = "$artist - $title"
}

data class TrackInfo(
        val url: String,
        val title: String,
        val artist: String,
        val coverURL: String?,
        val source: String
): Serializable {
    val name: String
        get() = "$artist - $title"
}