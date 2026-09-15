package com.chaoge.gymlog.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 动作库：一个动作一条（原计划动作、备选动作、自建动作都在这里） */
@Entity(tableName = "exercise", indices = [Index(value = ["name"], unique = true)])
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscle: String = "",
    val cues: String = "",
    val note: String = ""
)

/** 训练日类型：推日 / 拉日 / 腿日 / 用户新建的任意日 */
@Entity(tableName = "plan_day")
data class PlanDay(
    @PrimaryKey val key: String,      // "PUSH" / "PULL" / "LEG" / 新建时生成
    val label: String,                // "推日"
    val tag: String,                  // "胸 · 肩 · 三头"
    val sortOrder: Int
)

/** 计划动作模板：全局只一份，按 dayType 区分 */
@Entity(tableName = "plan_exercise")
data class PlanExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayType: String,          // 对应 plan_day.key
    val exerciseId: Long,         // 原计划动作
    val sortOrder: Int,
    val sets: Int,
    val minReps: Int,
    val maxReps: Int,
    val rpe: Int,
    val defaultRestSeconds: Int
)

/** 备选动作关联：某个计划动作的备选列表 */
@Entity(tableName = "alternative")
data class Alternative(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planExerciseId: Long,
    val exerciseId: Long,
    val sortOrder: Int,
    val isSystem: Boolean,
    val isHidden: Boolean = false
)

/** 训练记录：每天一条 */
@Entity(tableName = "workout_session", indices = [Index(value = ["dateKey"], unique = true)])
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,          // 例如 "2026-09-10"
    val dayType: String,          // plan_day.key
    val dayLabel: String = "",    // 当天日类型名快照（日被删/改名后历史仍正确显示）
    val startTime: Long? = null,  // 第一次点 √ 的时间戳
    val durationSeconds: Int = 0,
    val isCompleted: Boolean = false
)

/** 当天动作记录：一条计划动作对应一条 */
@Entity(tableName = "workout_exercise")
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val planExerciseId: Long,
    val exerciseName: String,     // 当前动作名快照
    val currentExerciseId: Long,  // 当前选中的动作 id
    val sortOrder: Int,
    val sets: Int,
    val isSkipped: Boolean = false,
    val note: String = ""
)

/** 每组记录 */
@Entity(tableName = "set_record")
data class SetRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean = false
)
