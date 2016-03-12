package org.seniorsigan.musicroom.usecases

import org.seniorsigan.musicroom.TrackInfo

interface SearchAPI {
    val sourceName: String
    fun search(query: String): List<TrackInfo>
}

class SearchFactory {
    private val sources: MutableMap<String, SearchAPI> = hashMapOf()

    fun register(source: String, api: SearchAPI): SearchFactory {
        sources[source] = api
        return this
    }

    fun engineFor(sourceList: List<String>?): SearchAPI {
        val s = sourceList ?: sources.keys.toList()
        return object : SearchAPI {
            override val sourceName: String
                get() = s.joinToString(", ")

            override fun search(query: String): List<TrackInfo> {
                return sources
                        .filterKeys { s.contains(it) }
                        .flatMap { it.value.search(query) }
            }
        }
    }
}