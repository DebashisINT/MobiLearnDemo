package com.breezemobilearndemo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.breezemobilearndemo.domain.LMSNotiDao
import com.breezemobilearndemo.domain.LMSNotiEntity


@Database(entities = arrayOf(LMSNotiEntity::class),
        version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lmsNotiDao(): LMSNotiDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun initAppDatabase(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "lms_db")

                        .allowMainThreadQueries()
                        .addMigrations()
                        .build()
            }
        }

        fun getDBInstance(): AppDatabase? {

            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

    }

}