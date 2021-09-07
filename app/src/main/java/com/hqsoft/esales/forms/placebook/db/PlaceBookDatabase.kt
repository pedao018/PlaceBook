package com.hqsoft.esales.forms.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hqsoft.esales.forms.placebook.model.Bookmark

// 1
@Database(entities = arrayOf(Bookmark::class), version = 2)
abstract class PlaceBookDatabase : RoomDatabase() {
    // 2
    abstract fun bookmarkDao(): BookmarkDao

    // 3
    companion object {
        // 4
        private var instance: PlaceBookDatabase? = null

        // 5
        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
                // 6
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceBookDatabase::class.java, "PlaceBook"
                )
                    //call the builder and tells Room to create a new empty database if it can’t find any Migrations.
                    .fallbackToDestructiveMigration()
                    .build()
            }
            // 7
            return instance as PlaceBookDatabase
        }
    }

}