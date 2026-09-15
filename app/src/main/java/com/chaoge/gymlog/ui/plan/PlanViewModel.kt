package com.chaoge.gymlog.ui.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.DayInfo
import com.chaoge.gymlog.domain.PlanItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlanViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as GymLogApplication).repository

    private val _days = MutableStateFlow<List<DayInfo>>(emptyList())
    val days: StateFlow<List<DayInfo>> = _days

    private val _day = MutableStateFlow<String?>(null)
    val day: StateFlow<String?> = _day

    private val _items = MutableStateFlow<List<PlanItem>>(emptyList())
    val items: StateFlow<List<PlanItem>> = _items

    init { reload() }

    fun reload() {
        viewModelScope.launch {
            val ds = repo.loadDays()
            _days.value = ds
            if (_day.value == null || ds.none { it.key == _day.value }) {
                _day.value = ds.firstOrNull()?.key
            }
            _items.value = _day.value?.let { repo.loadPlan(it) } ?: emptyList()
        }
    }

    fun selectDay(key: String) {
        _day.value = key
        reload()
    }

    fun createDay(label: String) {
        viewModelScope.launch {
            val d = repo.createDay(label)
            _day.value = d.key
            reload()
        }
    }

    fun deleteDay() {
        viewModelScope.launch {
            val key = _day.value ?: return@launch
            repo.deleteDay(key)
            reload()
        }
    }

    fun addExercise(name: String) {
        viewModelScope.launch {
            val key = _day.value ?: return@launch
            repo.addExerciseToPlan(key, name)
            reload()
        }
    }

    fun deleteExercise(planExerciseId: Long) {
        viewModelScope.launch {
            repo.deletePlanExercise(planExerciseId)
            reload()
        }
    }

    fun saveExercise(
        planExerciseId: Long, name: String, muscle: String,
        sets: Int, minReps: Int, maxReps: Int, rpe: Int, rest: Int, alts: List<String>
    ) {
        viewModelScope.launch {
            repo.updatePlanExercise(planExerciseId, name, muscle, sets, minReps, maxReps, rpe, rest, alts)
            reload()
        }
    }
}
