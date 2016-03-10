package org.seniorsigan.musicroom.usecases

import org.seniorsigan.musicroom.TrackInfo

interface SearchAPI {
    fun search(query: String, cb: (List<TrackInfo>) -> Unit)
}