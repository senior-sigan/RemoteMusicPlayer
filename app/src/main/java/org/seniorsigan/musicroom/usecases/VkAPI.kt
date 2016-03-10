package org.seniorsigan.musicroom.usecases

import org.seniorsigan.musicroom.TrackInfo

class VkAPI(): SearchAPI {
    override fun search(query: String, cb: (List<TrackInfo>) -> Unit) {
        throw UnsupportedOperationException()
    }
}