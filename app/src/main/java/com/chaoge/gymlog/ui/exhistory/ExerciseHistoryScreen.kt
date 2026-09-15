package com.chaoge.gymlog.ui.exhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.ExerciseHistoryEntry
import com.chaoge.gymlog.domain.fmtWeight
import com.chaoge.gymlog.ui.record.BackRow
import com.chaoge.gymlog.ui.theme.DividerColor
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.TextSecondary

@Composable
fun ExerciseHistoryScreen(exerciseId: Long, name: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { (context.applicationContext as GymLogApplication).repository }
    var entries by remember { mutableStateOf<List<ExerciseHistoryEntry>>(emptyList()) }
    LaunchedEffect(exerciseId) { entries = repo.exerciseHistory(exerciseId) }

    Column(Modifier.fillMaxSize().background(PageBg)) {
        BackRow(onBack)
        Text(name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(entries) { e ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("${e.dateKey} · ${e.dayLabel}", color = TextSecondary, fontSize = 13.sp)
                    e.rows.forEachIndexed { i, r ->
                        Text(
                            "第${i + 1}组　${fmtWeight(r.first)}kg × ${r.second}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
                HorizontalDivider(color = DividerColor)
            }
        }
    }
}
