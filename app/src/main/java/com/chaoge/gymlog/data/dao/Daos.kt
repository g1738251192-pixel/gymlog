package com.chaoge.gymlog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaoge.gymlog.data.entity.Alternative
import com.chaoge.gymlog.data.entity.Exercise
import com.chaoge.gymlog.data.entity.PlanDay
import com.chaoge.gymlog.data.entity.PlanExercise
import com.chaoge.gymlog.data.entity.SetRecord
import com.chaoge.gymlog.data.entity.WorkoutExercise
import com.chaoge.gymlog.data.entity.WorkoutSession

/** 动作历史查询结果行 */
data class SetHistoryRow(
    val dateKey: String,
    val dayLabel: String,
    val weightKg: Double,
    val reps: Int,
    val setIndex: Int
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise ORDER BY name")
    suspend fun getAll(): List<Exercise>

    @Query("SELECT * FROM exercise WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: Exercise): Long

    @Update
    suspend fun update(e: Exercise)
}

@Dao
interface PlanDayDao {
    @Query("SELECT * FROM plan_day ORDER BY sortOrder")
    suspend fun getAll(): List<PlanDay>

    @Query("SELECT * FROM plan_day WHERE key = :key")
    suspend fun get(key: String): PlanDay?

    @Query("SELECT COUNT(*) FROM plan_day")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(d: PlanDay)

    @Query("DELETE FROM plan_day WHERE key = :key")
    suspend fun delete(key: String)
}

@Dao
interface PlanDao {
    @Query("SELECT COUNT(*) FROM plan_exercise")
    suspend fun count(): Int

    @Query("SELECT * FROM plan_exercise ORDER BY dayType, sortOrder")
    suspend fun getAll(): List<PlanExercise>

    @Query("SELECT * FROM plan_exercise WHERE dayType = :day ORDER BY sortOrder")
    suspend fun getByDay(day: String): List<PlanExercise>

    @Query("SELECT * FROM plan_exercise WHERE id = :id")
    suspend fun getById(id: Long): PlanExercise?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(p: PlanExercise): Long

    @Update
    suspend fun update(p: PlanExercise)

    @Query("DELETE FROM plan_exercise WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM plan_exercise WHERE dayType = :day")
    suspend fun deleteByDay(day: String)

    @Query("DELETE FROM alternative WHERE planExerciseId = :peId")
    suspend fun clearAlts(peId: Long)

    @Query("SELECT * FROM alternative WHERE planExerciseId = :peId AND isHidden = 0 ORDER BY sortOrder")
    suspend fun alternatives(peId: Long): List<Alternative>

    @Query("SELECT * FROM alternative WHERE planExerciseId = :peId ORDER BY sortOrder")
    suspend fun allAlternatives(peId: Long): List<Alternative>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlt(a: Alternative): Long

    @Query("UPDATE alternative SET isHidden = :hidden WHERE id = :id")
    suspend fun setAltHidden(id: Long, hidden: Boolean)

    @Query("DELETE FROM alternative WHERE id = :id")
    suspend fun deleteAlt(id: Long)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_session WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getByDate(dateKey: String): WorkoutSession?

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getSession(id: Long): WorkoutSession?

    @Insert
    suspend fun insertSession(s: WorkoutSession): Long

    @Update
    suspend fun updateSession(s: WorkoutSession)

    @Query("UPDATE workout_session SET dayType = :type, dayLabel = :label WHERE id = :id")
    suspend fun updateDayType(id: Long, type: String, label: String)

    @Query("UPDATE workout_session SET isCompleted = 1, durationSeconds = :dur WHERE id = :id")
    suspend fun completeSession(id: Long, dur: Int)

    @Query("SELECT * FROM workout_session WHERE isCompleted = 1 ORDER BY dateKey DESC")
    suspend fun completedSessions(): List<WorkoutSession>

    @Query("SELECT dayType FROM workout_session WHERE isCompleted = 1 ORDER BY dateKey DESC LIMIT 1")
    suspend fun lastDayType(): String?

    @Query("SELECT * FROM workout_exercise WHERE sessionId = :sid ORDER BY sortOrder")
    suspend fun workoutExercises(sid: Long): List<WorkoutExercise>

    @Query("SELECT * FROM workout_exercise WHERE id = :id")
    suspend fun getWorkoutExercise(id: Long): WorkoutExercise?

    @Insert
    suspend fun insertWorkoutExercise(we: WorkoutExercise): Long

    @Update
    suspend fun updateWorkoutExercise(we: WorkoutExercise)

    @Query("DELETE FROM workout_exercise WHERE sessionId = :sid")
    suspend fun clearWorkoutExercises(sid: Long)

    @Query("UPDATE workout_exercise SET currentExerciseId = :eid, exerciseName = :name WHERE id = :id")
    suspend fun switchExercise(id: Long, eid: Long, name: String)

    @Query("UPDATE workout_exercise SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String)

    @Query("SELECT * FROM set_record WHERE workoutExerciseId = :weId ORDER BY setIndex")
    suspend fun setsFor(weId: Long): List<SetRecord>

    @Query("SELECT COUNT(*) FROM set_record sr JOIN workout_exercise we ON sr.workoutExerciseId = we.id WHERE we.sessionId = :sid AND sr.isCompleted = 1")
    suspend fun completedSetCount(sid: Long): Int

    @Insert
    suspend fun insertSet(r: SetRecord): Long

    @Query("DELETE FROM set_record WHERE workoutExerciseId = :weId")
    suspend fun clearSets(weId: Long)

    @Query("UPDATE set_record SET isCompleted = :done WHERE id = :id")
    suspend fun setCompleted(id: Long, done: Boolean)

    @Query("UPDATE set_record SET weightKg = :w WHERE id = :id")
    suspend fun setWeight(id: Long, w: Double)

    @Query("UPDATE set_record SET reps = :r WHERE id = :id")
    suspend fun setReps(id: Long, r: Int)

    /** 某个动作在历史（已完成、今天之前）里的所有组记录，按日期倒序 + 组序 */
    @Query(
        "SELECT ws.dateKey AS dateKey, ws.dayLabel AS dayLabel, sr.weightKg AS weightKg, " +
            "sr.reps AS reps, sr.setIndex AS setIndex " +
            "FROM set_record sr " +
            "JOIN workout_exercise we ON sr.workoutExerciseId = we.id " +
            "JOIN workout_session ws ON we.sessionId = ws.id " +
            "WHERE we.currentExerciseId = :eid AND ws.isCompleted = 1 AND ws.dateKey < :today " +
            "ORDER BY ws.dateKey DESC, sr.setIndex ASC"
    )
    suspend fun historyRows(eid: Long, today: String): List<SetHistoryRow>

    /** 某动作全部历史（供动作历史页用） */
    @Query(
        "SELECT ws.dateKey AS dateKey, ws.dayLabel AS dayLabel, sr.weightKg AS weightKg, " +
            "sr.reps AS reps, sr.setIndex AS setIndex " +
            "FROM set_record sr " +
            "JOIN workout_exercise we ON sr.workoutExerciseId = we.id " +
            "JOIN workout_session ws ON we.sessionId = ws.id " +
            "WHERE we.currentExerciseId = :eid AND ws.isCompleted = 1 " +
            "ORDER BY ws.dateKey DESC, sr.setIndex ASC"
    )
    suspend fun allHistoryRows(eid: Long): List<SetHistoryRow>
}
