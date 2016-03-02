package org.seniorsigan.musicroom

import android.graphics.Bitmap
import java.io.Serializable

class AudioPlayedMessage(): Serializable

data class AudioInfo(
        val artist: String?,
        val title: String?,
        val url: String,
        val album: String?,
        val picture: ByteArray?

): Serializable {
    val name = "$artist - $title"
}

data class TrackModel(
        val id: Int,
        val artist: String?,
        val title: String?,
        val url: String
): Serializable

data class CommonResponse(
        val success: Boolean,
        val error: String?,
        val data: Any?
): Serializable

data class TrackForm(
        val url: String = "",
        val title: String? = null,
        val artist: String? = null
): Serializable {
    val name: String
        get() = "$artist - $title"
}

data class Track(
        val url: String,
        val title: String,
        val artist: String,
        val cover: Bitmap
): Serializable {
    val name: String
        get() = "$artist - $title"
}