package org.seniorsigan.musicroom.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.seniorsigan.musicroom.TAG

const val TABLE_NAME = "history"
const val DATABASE_NAME = "music_room_database.db"
const val DATABASE_VERSION = 1

class DatabaseOpenHelper(val ctx: Context) : SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private var instance: DatabaseOpenHelper? = null

        fun getInstance(ctx: Context): DatabaseOpenHelper {
            synchronized(this, {
                if (instance == null) {
                    instance = DatabaseOpenHelper(ctx.applicationContext)
                }
                return instance!!
            })
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "DatabaseOpenHelper onCreate")
        db?.execSQL("""
            CREATE TABLE $TABLE_NAME (
                ${HistoryEntry.ID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ${HistoryEntry.URL} TEXT NOT NULL,
                ${HistoryEntry.TITLE} TEXT NOT NULL,
                ${HistoryEntry.ARTIST} TEXT NOT NULL,
                ${HistoryEntry.COVER_URL} TEXT,
                ${HistoryEntry.ALBUM} TEXT,
                ${HistoryEntry.SOURCE} TEXT,
                ${HistoryEntry.REMOTE_ID} TEXT,
                ${HistoryEntry.CREATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp,
                ${HistoryEntry.UPDATED_AT} TIMESTAMP NOT NULL DEFAULT current_timestamp
            )
        """)

        Log.d(TAG, "Create trigger $DATABASE_NAME.updated_at_trigger}")
        db?.execSQL("""
            CREATE TRIGGER updated_at_trigger
            AFTER UPDATE ON $TABLE_NAME
            FOR EACH ROW BEGIN
                UPDATE $TABLE_NAME
                SET ${HistoryEntry.UPDATED_AT} = current_timestamp
                WHERE ${HistoryEntry.ID} = old.${HistoryEntry.ID};
            END
        """)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrade database from $oldVersion to $newVersion version")
        throw UnsupportedOperationException()
    }
}