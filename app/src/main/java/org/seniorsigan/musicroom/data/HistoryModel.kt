package org.seniorsigan.musicroom.data

import java.util.*

data class HistoryModel(
        val _id: Long,
        val artist: String,
        val title: String,
        val url: String,
        val source: String,
        val coverURL: String?,
        val createdAt: Date = Date(),
        val updatedAt: Date = Date()
)