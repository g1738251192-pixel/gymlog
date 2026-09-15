package com.chaoge.gymlog.ui.done

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chaoge.gymlog.GymLogApplication
import com.chaoge.gymlog.domain.DoneSummary
import com.chaoge.gymlog.ui.theme.DoneColor
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.TextSecondary

@Composable
fun DoneScreen(dateKey: String, onViewRecord: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { (context.applicationContext as GymLogApplication).repository }
    var summary by remember { mutableStateOf<DoneSummary?>(null) }
    LaunchedEffect(dateKey) { summary = repo.loadDone(dateKey) }

    Column(Modifier.fillMaxSize().background(PageBg), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(64.dp))
        Box(Modifier.size(72.dp).clip(CircleShape).background(DoneColor), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("训练完成", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(summary?.dayLabel ?: "", color = TextSecondary, fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Stat("${(summary?.durationSeconds ?: 0) / 60}分钟", "训练时长")
            Stat("${summary?.completedSets ?: 0}组", "完成")
        }
        Spacer(Modifier.height(48.dp))
        OutlinedButton(
            onClick = onViewRecord,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, Primary)
        ) { Text("查看记录", color = Primary, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onDone) { Text("完成", color = Primary) }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
