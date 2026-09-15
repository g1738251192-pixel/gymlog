package com.chaoge.gymlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaoge.gymlog.ui.done.DoneScreen
import com.chaoge.gymlog.ui.exhistory.ExerciseHistoryScreen
import com.chaoge.gymlog.ui.history.HistoryScreen
import com.chaoge.gymlog.ui.navigation.Screen
import com.chaoge.gymlog.ui.plan.PlanScreen
import com.chaoge.gymlog.ui.record.RecordScreen
import com.chaoge.gymlog.ui.theme.GymLogTheme
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.TextSecondary
import com.chaoge.gymlog.ui.today.TodayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymLogTheme { GymLogApp() }
        }
    }
}

@Composable
fun GymLogApp() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Today) }
    val current = backStack.last()

    fun push(s: Screen) { backStack.add(s) }
    fun pop() { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }

    val showTabs = current is Screen.Today || current is Screen.Plan || current is Screen.History

    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            if (showTabs) {
                BottomBar(current, onSelect = { backStack.clear(); backStack.add(it) })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = current) {
                is Screen.Today -> TodayScreen(onFinished = { push(Screen.Done(it)) })
                is Screen.Plan -> PlanScreen()
                is Screen.History -> HistoryScreen(onOpenRecord = { push(Screen.Record(it)) })
                is Screen.Record -> RecordScreen(
                    s.dateKey,
                    onBack = { pop() },
                    onOpenExercise = { id, name -> push(Screen.ExerciseHistory(id, name)) }
                )
                is Screen.ExerciseHistory -> ExerciseHistoryScreen(s.exerciseId, s.name, onBack = { pop() })
                is Screen.Done -> DoneScreen(
                    s.dateKey,
                    onViewRecord = { push(Screen.Record(s.dateKey)) },
                    onDone = { backStack.clear(); backStack.add(Screen.Today) }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    Surface(color = Color.White) {
        Row(Modifier.fillMaxWidth().height(64.dp)) {
            TabItem("今日", Icons.Filled.FitnessCenter, current is Screen.Today) { onSelect(Screen.Today) }
            TabItem("计划", Icons.AutoMirrored.Filled.List, current is Screen.Plan) { onSelect(Screen.Plan) }
            TabItem("历史", Icons.Filled.CalendarMonth, current is Screen.History) { onSelect(Screen.History) }
        }
    }
}

@Composable
private fun RowScope.TabItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = if (selected) Primary else TextSecondary, modifier = Modifier.size(24.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = if (selected) Primary else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
