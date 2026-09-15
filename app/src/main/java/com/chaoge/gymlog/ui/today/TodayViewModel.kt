package com.chaoge.gymlog.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.AltEntry
import com.chaoge.gymlog.domain.DayInfo
import com.chaoge.gymlog.domain.ExerciseItem
import com.chaoge.gymlog.domain.SetRow
import com.chaoge.gymlog.domain.TodayData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as GymLogApplication).repository

    private val _data = MutableStateFlow<TodayData?>(null)
    val data: StateFlow<TodayData?> = _data

    private val _days = MutableStateFlow<List<DayInfo>>(emptyList())
    val days: StateFlow<List<DayInfo>> = _days

    private val _restLeft = MutableStateFlow<Int?>(null)
    val restLeft: StateFlow<Int?> = _restLeft

    private val _alts = MutableStateFlow<List<AltEntry>>(emptyList())
    val alts: StateFlow<List<AltEntry>> = _alts

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private var restJob: Job? = null

    val todayKey: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            repo.ensureTodaySession(todayKey)
            refresh()
        }
        viewModelScope.launch {
            repo.planVersion.collect { reloadFromPlan() }
        }
    }

    private suspend fun refresh() {
        _days.value = repo.loadDays()
        _data.value = repo.loadToday(todayKey)
        _loading.value = false
    }

    private suspend fun reloadFromPlan() {
        if (_data.value == null) return
        _days.value = repo.loadDays()
        repo.syncTodayWithPlan(todayKey)
        _data.value = repo.loadToday(todayKey)
    }

    fun switchDay(dayKey: String) {
        viewModelScope.launch {
            val s = _data.value ?: return@launch
            repo.switchDayType(s.sessionId, dayKey)
            refresh()
        }
    }

    fun toggleSet(item: ExerciseItem, row: SetRow) {
        val done = !row.isCompleted
        viewModelScope.launch {
            val s = _data.value ?: return@launch
            repo.setCompleted(s.sessionId, row.setRecordId, done)
            refresh()
            if (done) startRest(item.restSeconds)
        }
    }

    fun setWeight(row: SetRow, w: Double) {
        viewModelScope.launch { repo.setWeight(row.setRecordId, w); refresh() }
    }

    fun setReps(row: SetRow, r: Int) {
        viewModelScope.launch { repo.setReps(row.setRecordId, r); refresh() }
    }

    fun switchExercise(item: ExerciseItem, newExerciseId: Long) {
        viewModelScope.launch {
            repo.switchExercise(item.workoutExerciseId, newExerciseId)
            refresh()
        }
    }

    fun setNote(item: ExerciseItem, note: String) {
        viewModelScope.launch { repo.setNote(item.workoutExerciseId, note); refresh() }
    }

    fun addAlternative(item: ExerciseItem, name: String) {
        viewModelScope.launch {
            val id = repo.addAlternative(item.planExerciseId, name)
            if (id > 0) repo.switchExercise(item.workoutExerciseId, id)
            refresh()
        }
    }

    fun finish(onFinished: (String) -> Unit) {
        viewModelScope.launch {
            val s = _data.value ?: return@launch
            repo.finishSession(s.sessionId)
            stopRest()
            onFinished(todayKey)
        }
    }

    fun loadAlternatives(planExerciseId: Long) {
        viewModelScope.launch { _alts.value = repo.loadAlternatives(planExerciseId) }
    }

    fun deleteAlt(altId: Long, planExerciseId: Long) {
        viewModelScope.launch {
            repo.deleteAlternative(altId)
            _alts.value = repo.loadAlternatives(planExerciseId)
            refresh()
        }
    }

    fun hideAlt(altId: Long, planExerciseId: Long) {
        viewModelScope.launch {
            repo.hideAlternative(altId)
            _alts.value = repo.loadAlternatives(planExerciseId)
            refresh()
        }
    }

    fun addThirty() { _restLeft.value = (_restLeft.value ?: 0) + 30 }
    fun skipRest() = stopRest()

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        _restLeft.value = seconds
        restJob = viewModelScope.launch {
            while ((_restLeft.value ?: 0) > 0) {
                delay(1000)
                _restLeft.value = (_restLeft.value ?: 1) - 1
            }
            _restLeft.value = null
        }
    }

    private fun stopRest() {
        restJob?.cancel()
        restJob = null
        _restLeft.value = null
    }

    override fun onCleared() {
        super.onCleared()
        restJob?.cancel()
    }
}
