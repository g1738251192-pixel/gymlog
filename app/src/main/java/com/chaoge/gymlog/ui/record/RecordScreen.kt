package com.chaoge.gymlog.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.RecordData
import com.chaoge.gymlog.domain.RecordExercise
import com.chaoge.gymlog.domain.fmtWeight
import com.chaoge.gymlog.ui.theme.DividerColor
import com.chaoge.gymlog.ui.theme.DoneColor
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.TextPrimary
import com.chaoge.gymlog.ui.theme.TextSecondary
import com.chaoge.gymlog.ui.theme.WarnColor

@Composable
fun RecordScreen(dateKey: String, onBack: () -> Unit, onOpenExercise: (Long, String) -> Unit) {
    val context = LocalContext.current
    val repo = remember { (context.applicationContext as GymLogApplication).repository }
    var data by remember { mutableStateOf<RecordData?>(null) }
    LaunchedEffect(dateKey) { data = repo.loadRecord(dateKey) }

    Column(Modifier.fillMaxSize().background(PageBg)) {
        BackRow(onBack)
        val d = data
        if (d == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        } else {
            Text(d.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(d.items) { ex -> RecordExerciseView(ex, onOpenExercise) }
            }
        }
    }
}

@Composable
private fun RecordExerciseView(ex: RecordExercise, onOpenExercise: (Long, String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                ex.name,
                color = Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onOpenExercise(ex.exerciseId, ex.name) }
            )
            Text(if (ex.isSkipped) "已跳过" else "✓ 完成", color = if (ex.isSkipped) WarnColor else DoneColor, fontSize = 13.sp)
        }
        ex.rows.forEachIndexed { i, r ->
            Text(
                "第${i + 1}组　${fmtWeight(r.first)}kg × ${r.second}",
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (ex.note.isNotBlank()) {
            Text("📝 ${ex.note}", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
    HorizontalDivider(color = DividerColor)
}

@Composable
internal fun BackRow(onBack: () -> Unit) {
    Row(
        Modifier.clickable(onClick = onBack).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Primary, modifier = Modifier.size(20.dp))
        Text("返回", color = Primary, fontSize = 14.sp)
    }
}
