package com.example.shareeat.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shareeat.base.MyApplication
import com.example.shareeat.model.User

@Database(entities = [User::class], version = 3)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun userDao(): UserDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}