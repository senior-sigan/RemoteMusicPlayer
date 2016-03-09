package org.seniorsigan.musicroom.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.seniorsigan.musicroom.TAG
import java.text.SimpleDateFormat
import java.util.*

class HistoryRepository(val db: SQLiteOpenHelper) {
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val projection = arrayOf(
            HistoryEntry.ID,
            HistoryEntry.URL,
            HistoryEntry.TITLE,
            HistoryEntry.ARTIST,
            HistoryEntry.COVER_URL,
            HistoryEntry.ALBUM,
            HistoryEntry.SOURCE,
            HistoryEntry.REMOTE_ID,
            HistoryEntry.CREATED_AT,
            HistoryEntry.UPDATED_AT
    )

    init {
        iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun create(artist: String, title: String, url: String, source: String): HistoryModel {
        Log.d(TAG, "HistoryRepository::create")
        var model: HistoryModel
        db.writableDatabase.beginTransaction()
        try {
            val values = ContentValues()
            values.put(HistoryEntry.ARTIST, artist)
            values.put(HistoryEntry.TITLE, title)
            values.put(HistoryEntry.URL, url)
            values.put(HistoryEntry.SOURCE, source)
            val id = db.writableDatabase.insertOrThrow(TABLE_NAME, null, values)
            model = HistoryModel(_id = id, artist = artist, title = title, url = url)
            db.writableDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception("Something went wrong while saving history $artist-$title : ${e.message}", e)
        } finally {
            db.writableDatabase.endTransaction()
        }
        Log.d(TAG, "HistoryRepository created $model")
        EventBus.getDefault().post(model)
        return model
    }

    fun findAll(): List<HistoryModel> {
        Log.d(TAG, "HistoryRepository::findAll")
        val cursor = db.readableDatabase.query(TABLE_NAME, projection, null, null, null, null, null, null)
        return convert(cursor)
    }

    private fun convert(cursor: Cursor?): List<HistoryModel> {
        val result: MutableList<HistoryModel> = arrayListOf()
        if (cursor != null) {
            Log.d(TAG, "Loaded ${cursor.count} elements")
            if (cursor.moveToFirst()) {
                do {
                    result.add(HistoryModel(
                            _id = cursor.getLong(0),
                            url = cursor.getString(1),
                            title = cursor.getString(2),
                            artist = cursor.getString(3),
                            coverURL = cursor.getString(4),
                            createdAt = iso8601Format.parse(cursor.getString(8)),
                            updatedAt = iso8601Format.parse(cursor.getString(9))
                    ))
                } while (cursor.moveToNext())
            }
        } else {
            Log.d(TAG, "Loaded 0 elements")
        }

        return result
    }
}