package com.chaoge.gymlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chaoge.gymlog.data.dao.ExerciseDao
import com.chaoge.gymlog.data.dao.PlanDao
import com.chaoge.gymlog.data.dao.PlanDayDao
import com.chaoge.gymlog.data.dao.WorkoutDao
import com.chaoge.gymlog.data.entity.Alternative
import com.chaoge.gymlog.data.entity.Exercise
import com.chaoge.gymlog.data.entity.PlanDay
import com.chaoge.gymlog.data.entity.PlanExercise
import com.chaoge.gymlog.data.entity.SetRecord
import com.chaoge.gymlog.data.entity.WorkoutExercise
import com.chaoge.gymlog.data.entity.WorkoutSession

@Database(
    entities = [
        Exercise::class,
        PlanDay::class,
        PlanExercise::class,
        Alternative::class,
        WorkoutSession::class,
        WorkoutExercise::class,
        SetRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun planDayDao(): PlanDayDao
    abstract fun planDao(): PlanDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymlog.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
