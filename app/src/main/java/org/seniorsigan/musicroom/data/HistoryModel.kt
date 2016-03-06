package org.seniorsigan.musicroom.data

import java.util.*

class HistoryModel(
        val _id: Long,
        val artist: String,
        val title: String,
        val url: String,
        val coverURL: String? = null,
        val createdAt: Date = Date(),
        val updatedAt: Date = Date()
)