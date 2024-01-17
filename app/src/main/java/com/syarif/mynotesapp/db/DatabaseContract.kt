package com.syarif.mynotesapp.db

import android.provider.BaseColumns

internal class DatabaseContract {
    internal class NoteColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "note"
            const val _ID = "_id"
            const val TITLE = "TITLE"
            const val DESCRIPTION = "description"
            const val DATE = "date"
        }
    }
}