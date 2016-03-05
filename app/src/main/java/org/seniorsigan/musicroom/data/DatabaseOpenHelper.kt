package org.seniorsigan.musicroom.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.anko.db.*
import org.seniorsigan.musicroom.TAG

private const val TABLE_NAME = "history"
private const val DATABASE_NAME = "music_room_database.db"
private const val DATABASE_VERSION = 1

class DatabaseOpenHelper(val ctx: Context) : ManagedSQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {
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

    override fun onCreate(db: SQLiteDatabase) {
        use {
            Log.d(TAG, "Create table $DATABASE_NAME.$TABLE_NAME")
            createTable(TABLE_NAME, true,
                    HistoryEntry.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                    HistoryEntry.URL to TEXT,
                    HistoryEntry.TITLE to TEXT,
                    HistoryEntry.ARTIST to TEXT,
                    HistoryEntry.COVER_URL to TEXT,
                    HistoryEntry.ALBUM to TEXT,
                    HistoryEntry.SOURCE to TEXT,
                    HistoryEntry.REMOTE_ID to TEXT,
                    HistoryEntry.CREATED_AT to TEXT,
                    HistoryEntry.UPDATED_AT to TEXT)

            Log.d(TAG, "Create trigger $DATABASE_NAME.updated_at_trigger}")
            execSQL("""
                CREATE TRIGGER updated_at_trigger
                AFTER UPDATE ON $TABLE_NAME
                FOR EACH ROW BEGIN
                    UPDATE $TABLE_NAME
                    SET ${HistoryEntry.UPDATED_AT} = current_timestamp
                    WHERE ${HistoryEntry.ID} = old.${HistoryEntry.ID};
                END
            """)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrade database from $oldVersion to $newVersion version")
        throw UnsupportedOperationException()
    }
}

val Context.database: DatabaseOpenHelper
    get() = DatabaseOpenHelper.getInstance(applicationContext)