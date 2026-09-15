package com.chaoge.gymlog.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chaoge.gymlog.domain.DayCell
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.TextPrimary
import com.chaoge.gymlog.ui.theme.TextSecondary

@Composable
fun HistoryScreen(onOpenRecord: (String) -> Unit) {
    val vm: HistoryViewModel = viewModel()
    val cells by vm.cells.collectAsState()
    val year by vm.year.collectAsState()
    val month by vm.month.collectAsState()

    Column(Modifier.fillMaxSize().background(PageBg)) {
        Text(
            "历史",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.prev() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Primary) }
            Text(
                "${year}年${month}月",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { vm.next() }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Primary) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = TextSecondary, fontSize = 12.sp)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(cells) { cell -> DayCellView(cell, onOpenRecord) }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, onOpenRecord: (String) -> Unit) {
    Box(
        Modifier.aspectRatio(1f)
            .then(if (cell.hasWorkout) Modifier.clickable { onOpenRecord(cell.dateKey) } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (cell.dayOfMonth != 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(if (cell.isToday) Primary else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${cell.dayOfMonth}",
                        color = if (cell.isToday) Color.White else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (cell.hasWorkout) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(Primary).padding(top = 2.dp))
                }
            }
        }
    }
}
