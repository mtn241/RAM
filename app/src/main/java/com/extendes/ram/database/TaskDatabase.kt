package com.extendes.ram.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =[TaskEntity::class, ContactListItem::class, ListItem::class],version = 2)
abstract class TaskDatabase:RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun listDao(): ListDao
    abstract fun contactListDao(): ContactListDao
    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getTaskDataBase(context: Context): TaskDatabase {
            synchronized(this){
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        TaskDatabase::class.java,
                        "taskDB"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }

        }

    }
}