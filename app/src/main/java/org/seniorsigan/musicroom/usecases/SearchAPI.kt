package org.seniorsigan.musicroom.usecases

import org.seniorsigan.musicroom.TrackInfo

interface SearchAPI {
    val sourceName: String
    fun search(query: String, cb: (success: Boolean, data: List<TrackInfo>) -> Unit)
}