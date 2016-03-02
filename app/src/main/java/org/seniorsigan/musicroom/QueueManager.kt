package org.seniorsigan.musicroom

class QueueManager {
    private var queue: Track? = null

    fun add(track: TrackForm) {
        queue = Track(
                url = track.url,
                title = track.title ?: "Unknown title",
                artist = track.artist ?: "Unknown artist")
    }

    fun current(): Track? {
        return queue
    }

    fun next() {
        queue = null
    }
}