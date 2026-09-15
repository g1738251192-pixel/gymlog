package com.chaoge.gymlog.data

import com.chaoge.gymlog.data.dao.SetHistoryRow
import com.chaoge.gymlog.data.entity.Alternative
import com.chaoge.gymlog.data.entity.Exercise
import com.chaoge.gymlog.data.entity.PlanDay
import com.chaoge.gymlog.data.entity.PlanExercise
import com.chaoge.gymlog.data.entity.SetRecord
import com.chaoge.gymlog.data.entity.WorkoutExercise
import com.chaoge.gymlog.data.entity.WorkoutSession
import com.chaoge.gymlog.domain.AltEntry
import com.chaoge.gymlog.domain.Candidate
import com.chaoge.gymlog.domain.DayCell
import com.chaoge.gymlog.domain.DayInfo
import com.chaoge.gymlog.domain.DoneSummary
import com.chaoge.gymlog.domain.ExerciseHistoryEntry
import com.chaoge.gymlog.domain.ExerciseItem
import com.chaoge.gymlog.domain.PlanItem
import com.chaoge.gymlog.domain.RecordData
import com.chaoge.gymlog.domain.RecordExercise
import com.chaoge.gymlog.domain.SetRow
import com.chaoge.gymlog.domain.TodayData
import com.chaoge.gymlog.domain.fmtWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

class Repository(private val db: AppDatabase) {
    private val exerciseDao = db.exerciseDao()
    private val planDayDao = db.planDayDao()
    private val planDao = db.planDao()
    private val workoutDao = db.workoutDao()

    private val _planVersion = MutableStateFlow(0)
    val planVersion: StateFlow<Int> = _planVersion
    private fun planChanged() { _planVersion.value++ }

    suspend fun ensureSeeded() = seedIfNeeded(planDayDao, planDao, exerciseDao)

    // ============ 训练日 ============
    suspend fun loadDays(): List<DayInfo> = planDayDao.getAll().map { DayInfo(it.key, it.label, it.tag) }

    suspend fun createDay(label: String): DayInfo {
        val key = "d" + System.currentTimeMillis()
        val order = planDayDao.getAll().size
        val d = PlanDay(key, label, "自定义", order)
        planDayDao.insert(d)
        planChanged()
        return DayInfo(d.key, d.label, d.tag)
    }

    suspend fun deleteDay(key: String) {
        planDayDao.delete(key)
        planDao.deleteByDay(key)
        planChanged()
    }

    // ============ 计划 ============
    suspend fun loadPlan(day: String): List<PlanItem> =
        planDao.getByDay(day).map { pe ->
            val ex = exerciseDao.getById(pe.exerciseId)
            val alts = planDao.alternatives(pe.id).mapNotNull { exerciseDao.getById(it.exerciseId)?.name }
            PlanItem(
                pe.id, ex?.name ?: "", ex?.muscle ?: "",
                pe.sets, pe.minReps, pe.maxReps, pe.rpe, pe.defaultRestSeconds, alts
            )
        }

    suspend fun addExerciseToPlan(day: String, name: String) {
        val exId = ensureExercise(name)
        val order = planDao.getByDay(day).size
        planDao.insert(
            PlanExercise(dayType = day, exerciseId = exId, sortOrder = order, sets = 3, minReps = 8, maxReps = 12, rpe = 8, defaultRestSeconds = 90)
        )
        planChanged()
    }

    suspend fun deletePlanExercise(planExerciseId: Long) {
        planDao.clearAlts(planExerciseId)
        planDao.deleteById(planExerciseId)
        planChanged()
    }

    suspend fun updatePlanExercise(
        planExerciseId: Long, name: String, muscle: String,
        sets: Int, minReps: Int, maxReps: Int, rpe: Int, rest: Int, alts: List<String>
    ) {
        val pe = planDao.getById(planExerciseId) ?: return
        exerciseDao.getById(pe.exerciseId)?.let { ex ->
            if (ex.name != name || ex.muscle != muscle) {
                exerciseDao.update(ex.copy(name = name, muscle = muscle))
            }
        }
        planDao.update(pe.copy(sets = sets, minReps = minReps, maxReps = maxReps, rpe = rpe, defaultRestSeconds = rest))
        planDao.clearAlts(pe.id)
        alts.forEachIndexed { i, altName ->
            val id = ensureExercise(altName)
            if (id > 0) planDao.insertAlt(Alternative(planExerciseId = pe.id, exerciseId = id, sortOrder = i, isSystem = false))
        }
        planChanged()
    }

    suspend fun loadAlternatives(peId: Long): List<AltEntry> =
        planDao.allAlternatives(peId).mapNotNull { a ->
            exerciseDao.getById(a.exerciseId)?.let { AltEntry(a.id, it.name, a.isSystem) }
        }

    suspend fun deleteAlternative(altId: Long) {
        planDao.deleteAlt(altId)
        planChanged()
    }

    suspend fun hideAlternative(altId: Long) {
        planDao.setAltHidden(altId, true)
        planChanged()
    }

    /** 首页「＋添加动作」：新建动作并加入备选，返回新动作 id */
    suspend fun addAlternative(planExerciseId: Long, name: String): Long {
        val id = ensureExercise(name)
        if (id > 0) planDao.insertAlt(Alternative(planExerciseId = planExerciseId, exerciseId = id, sortOrder = 999, isSystem = false))
        planChanged()
        return id
    }

    // ============ 今日 ============
    suspend fun ensureTodaySession(dateKey: String): WorkoutSession {
        var s = workoutDao.getByDate(dateKey)
        if (s == null) {
            val days = planDayDao.getAll()
            val last = workoutDao.lastDayType()
            val dayKey = if (last != null && days.any { it.key == last }) last else (days.firstOrNull()?.key ?: "PUSH")
            val day = planDayDao.get(dayKey) ?: PlanDay(dayKey, dayKey, "", 0)
            val id = workoutDao.insertSession(WorkoutSession(dateKey = dateKey, dayType = dayKey, dayLabel = day.label))
            s = workoutDao.getSession(id)!!
        }
        if (workoutDao.workoutExercises(s.id).isEmpty()) {
            rebuild(s.id, s.dayType, dateKey)
        }
        return workoutDao.getSession(s.id)!!
    }

    suspend fun loadToday(dateKey: String): TodayData {
        val s = workoutDao.getByDate(dateKey)
        if (s == null) {
            val first = planDayDao.getAll().firstOrNull()
            return TodayData(dateKey, first?.key ?: "", first?.label ?: "", first?.tag ?: "", 0, emptyList(), 0, 0, 0)
        }
        val day = planDayDao.get(s.dayType)
        val items = workoutDao.workoutExercises(s.id).map { buildItem(it, dateKey) }
        val total = items.sumOf { it.setRows.size }
        val done = items.sumOf { it.setRows.count { it.isCompleted } }
        val elapsed = s.startTime?.let { ((System.currentTimeMillis() - it) / 1000).toInt() } ?: 0
        return TodayData(dateKey, s.dayType, day?.label ?: s.dayLabel, day?.tag ?: "", s.id, items, done, total, elapsed)
    }

    suspend fun switchDayType(sessionId: Long, dayKey: String) {
        val day = planDayDao.get(dayKey) ?: return
        workoutDao.updateDayType(sessionId, dayKey, day.label)
        val s = workoutDao.getSession(sessionId) ?: return
        rebuild(sessionId, dayKey, s.dateKey)
    }

    suspend fun setCompleted(sessionId: Long, setRecordId: Long, done: Boolean) {
        workoutDao.setCompleted(setRecordId, done)
        if (done) {
            val s = workoutDao.getSession(sessionId)
            if (s != null && s.startTime == null) {
                workoutDao.updateSession(s.copy(startTime = System.currentTimeMillis()))
            }
        }
    }

    suspend fun setWeight(setRecordId: Long, w: Double) = workoutDao.setWeight(setRecordId, w)
    suspend fun setReps(setRecordId: Long, r: Int) = workoutDao.setReps(setRecordId, r)
    suspend fun setNote(weId: Long, note: String) = workoutDao.setNote(weId, note)

    suspend fun switchExercise(weId: Long, newExerciseId: Long) {
        val we = workoutDao.getWorkoutExercise(weId) ?: return
        val ex = exerciseDao.getById(newExerciseId) ?: return
        val session = workoutDao.getSession(we.sessionId) ?: return
        val pe = planDao.getById(we.planExerciseId)
        workoutDao.switchExercise(weId, newExerciseId, ex.name)
        workoutDao.clearSets(weId)
        buildSets(weId, we.sets, newExerciseId, session.dateKey, pe?.maxReps ?: 8)
    }

    suspend fun finishSession(sessionId: Long) {
        val s = workoutDao.getSession(sessionId) ?: return
        val dur = s.startTime?.let { ((System.currentTimeMillis() - it) / 1000).toInt() } ?: 0
        workoutDao.completeSession(sessionId, dur)
    }

    /** 计划变更后同步今日：未开始练则重建，已开始则保留 */
    suspend fun syncTodayWithPlan(dateKey: String) {
        val s = workoutDao.getByDate(dateKey) ?: return
        if (s.isCompleted) return
        if (workoutDao.completedSetCount(s.id) > 0) return
        rebuild(s.id, s.dayType, dateKey)
    }

    // ============ 历史 ============
    suspend fun calendarDays(year: Int, month: Int): List<DayCell> {
        val dateSet = workoutDao.completedSessions().map { it.dateKey }.toSet()
        val ym = YearMonth.of(year, month)
        val first = ym.atDay(1).dayOfWeek.value % 7 // 周日=0
        val cells = mutableListOf<DayCell>()
        repeat(first) { cells.add(DayCell("", 0, false, false)) }
        for (d in 1..ym.lengthOfMonth()) {
            val key = String.format("%04d-%02d-%02d", year, month, d)
            val isToday = key == LocalDate.now().toString()
            cells.add(DayCell(key, d, dateSet.contains(key), isToday))
        }
        return cells
    }

    suspend fun loadRecord(dateKey: String): RecordData? {
        val s = workoutDao.getByDate(dateKey) ?: return null
        if (!s.isCompleted) return null
        val items = workoutDao.workoutExercises(s.id).map { we ->
            val rows = workoutDao.setsFor(we.id).map { it.weightKg to it.reps }
            RecordExercise(we.exerciseName, we.currentExerciseId, we.isSkipped, we.note, rows)
        }
        return RecordData("${dateKey} · ${s.dayLabel.ifBlank { s.dayType }}", items)
    }

    suspend fun exerciseHistory(eid: Long): List<ExerciseHistoryEntry> {
        val rows = workoutDao.allHistoryRows(eid)
        val grouped = LinkedHashMap<String, MutableList<Pair<Double, Int>>>()
        val labelMap = HashMap<String, String>()
        for (r in rows) {
            grouped.getOrPut(r.dateKey) { mutableListOf() }.add(r.weightKg to r.reps)
            labelMap[r.dateKey] = r.dayLabel
        }
        return grouped.map { (date, list) -> ExerciseHistoryEntry(date, labelMap[date] ?: "", list) }
    }

    suspend fun loadDone(dateKey: String): DoneSummary? {
        val s = workoutDao.getByDate(dateKey) ?: return null
        val done = workoutDao.completedSetCount(s.id)
        return DoneSummary(s.dayLabel.ifBlank { s.dayType }, s.durationSeconds, done)
    }

    // ============ 内部 ============
    private suspend fun rebuild(sessionId: Long, dayType: String, dateKey: String) {
        workoutDao.clearWorkoutExercises(sessionId)
        planDao.getByDay(dayType).forEachIndexed { i, pe ->
            val ex = exerciseDao.getById(pe.exerciseId)
            val weId = workoutDao.insertWorkoutExercise(
                WorkoutExercise(
                    sessionId = sessionId, planExerciseId = pe.id,
                    exerciseName = ex?.name ?: "", currentExerciseId = pe.exerciseId,
                    sortOrder = i, sets = pe.sets
                )
            )
            buildSets(weId, pe.sets, pe.exerciseId, dateKey, pe.maxReps)
        }
    }

    private suspend fun buildSets(weId: Long, sets: Int, eid: Long, dateKey: String, defaultReps: Int) {
        val last = lastSessionRows(eid, dateKey)
        for (i in 0 until sets) {
            val w = if (i < last.size) last[i].weightKg else (last.lastOrNull()?.weightKg ?: 0.0)
            val r = if (i < last.size) last[i].reps else (last.lastOrNull()?.reps ?: defaultReps)
            workoutDao.insertSet(SetRecord(workoutExerciseId = weId, setIndex = i, weightKg = w, reps = r, isCompleted = false))
        }
    }

    private suspend fun lastSessionRows(eid: Long, today: String): List<SetHistoryRow> {
        val rows = workoutDao.historyRows(eid, today)
        if (rows.isEmpty()) return emptyList()
        val first = rows.first().dateKey
        return rows.filter { it.dateKey == first }
    }

    private suspend fun ensureExercise(name: String): Long {
        exerciseDao.getByName(name)?.let { return it.id }
        return exerciseDao.insert(Exercise(name = name))
    }

    private suspend fun buildCandidates(peId: Long, currentExerciseId: Long): List<Candidate> {
        val list = mutableListOf<Candidate>()
        planDao.getById(peId)?.let { pe ->
            exerciseDao.getById(pe.exerciseId)?.let { list.add(Candidate(it.id, it.name)) }
        }
        planDao.alternatives(peId).forEach { a ->
            exerciseDao.getById(a.exerciseId)?.let { list.add(Candidate(it.id, it.name)) }
        }
        if (list.none { it.exerciseId == currentExerciseId }) {
            exerciseDao.getById(currentExerciseId)?.let { list.add(Candidate(it.id, it.name)) }
        }
        return list
    }

    private suspend fun buildItem(we: WorkoutExercise, today: String): ExerciseItem {
        val ex = exerciseDao.getById(we.currentExerciseId)
        val pe = planDao.getById(we.planExerciseId)
        val candidates = buildCandidates(we.planExerciseId, we.currentExerciseId)
        val currentIndex = candidates.indexOfFirst { it.exerciseId == we.currentExerciseId }.coerceAtLeast(0)
        val setRows = workoutDao.setsFor(we.id).map {
            SetRow(it.id, it.setIndex, it.weightKg, it.reps, it.isCompleted)
        }
        val last = lastSessionRows(we.currentExerciseId, today)
        val lastLine = last.firstOrNull()?.let {
            fmtWeight(it.weightKg) + " × " + last.joinToString(" / ") { r -> "${r.reps}" }
        }
        return ExerciseItem(
            workoutExerciseId = we.id,
            planExerciseId = we.planExerciseId,
            exerciseId = we.currentExerciseId,
            name = ex?.name ?: we.exerciseName,
            muscle = ex?.muscle ?: "",
            cues = ex?.cues ?: "",
            note = we.note,
            sets = we.sets,
            minReps = pe?.minReps ?: 0,
            maxReps = pe?.maxReps ?: 0,
            rpe = pe?.rpe ?: 0,
            restSeconds = pe?.defaultRestSeconds ?: 90,
            isSkipped = we.isSkipped,
            lastLine = lastLine,
            candidates = candidates,
            currentIndex = currentIndex,
            setRows = setRows
        )
    }
}
