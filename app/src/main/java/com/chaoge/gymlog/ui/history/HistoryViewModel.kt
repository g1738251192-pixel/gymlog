package com.chaoge.gymlog.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.DayCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as GymLogApplication).repository

    private val _cells = MutableStateFlow<List<DayCell>>(emptyList())
    val cells: StateFlow<List<DayCell>> = _cells

    private val _year = MutableStateFlow(LocalDate.now().year)
    val year: StateFlow<Int> = _year

    private val _month = MutableStateFlow(LocalDate.now().monthValue)
    val month: StateFlow<Int> = _month

    init { reload() }

    fun prev() {
        if (_month.value == 1) { _month.value = 12; _year.value -= 1 } else { _month.value -= 1 }
        reload()
    }

    fun next() {
        if (_month.value == 12) { _month.value = 1; _year.value += 1 } else { _month.value += 1 }
        reload()
    }

    private fun reload() {
        viewModelScope.launch { _cells.value = repo.calendarDays(_year.value, _month.value) }
    }
}
