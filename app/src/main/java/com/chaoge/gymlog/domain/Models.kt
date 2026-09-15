package com.chaoge.gymlog.domain

/** 训练日类型（UI 用） */
data class DayInfo(val key: String, val label: String, val tag: String)

/** 每组记录（UI 用） */
data class SetRow(
    val setRecordId: Long,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val isCompleted: Boolean
)

/** 候选动作（切换用）：原计划 + 备选 */
data class Candidate(val exerciseId: Long, val name: String)

/** 今日页的一个动作块 */
data class ExerciseItem(
    val workoutExerciseId: Long,
    val planExerciseId: Long,
    val exerciseId: Long,
    val name: String,
    val muscle: String,
    val cues: String,
    val note: String,
    val sets: Int,
    val minReps: Int,
    val maxReps: Int,
    val rpe: Int,
    val restSeconds: Int,
    val isSkipped: Boolean,
    val lastLine: String?,
    val candidates: List<Candidate>,
    val currentIndex: Int,
    val setRows: List<SetRow>
)

data class TodayData(
    val dateKey: String,
    val dayType: String,
    val dayLabel: String,
    val dayTag: String,
    val sessionId: Long,
    val items: List<ExerciseItem>,
    val completedSets: Int,
    val totalSets: Int,
    val elapsedSeconds: Int
)

/** 计划页的一项 */
data class PlanItem(
    val planExerciseId: Long,
    val name: String,
    val muscle: String,
    val sets: Int,
    val minReps: Int,
    val maxReps: Int,
    val rpe: Int,
    val restSeconds: Int,
    val alternatives: List<String>
)

/** 备选动作（编辑用） */
data class AltEntry(val altId: Long, val name: String, val isSystem: Boolean)

/** 动作历史页的一组日期记录 */
data class ExerciseHistoryEntry(
    val dateKey: String,
    val dayLabel: String,
    val rows: List<Pair<Double, Int>>
)

/** 历史记录页（当天训练痕迹） */
data class RecordData(
    val title: String,
    val items: List<RecordExercise>
)

data class RecordExercise(
    val name: String,
    val exerciseId: Long,
    val isSkipped: Boolean,
    val note: String,
    val rows: List<Pair<Double, Int>>
)

/** 完成页摘要 */
data class DoneSummary(val dayLabel: String, val durationSeconds: Int, val completedSets: Int)

/** 日历里的一天 */
data class DayCell(
    val dateKey: String,
    val dayOfMonth: Int,
    val hasWorkout: Boolean,
    val isToday: Boolean
)

/** 重量显示：整数不带小数点，带小数的保留 */
fun fmtWeight(w: Double): String =
    if (w == w.toLong().toDouble()) w.toLong().toString() else w.toString()
