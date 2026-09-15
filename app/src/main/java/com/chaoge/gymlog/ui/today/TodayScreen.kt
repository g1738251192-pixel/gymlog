package com.chaoge.gymlog.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chaoge.gymlog.domain.AltEntry
import com.chaoge.gymlog.domain.DayInfo
import com.chaoge.gymlog.domain.ExerciseItem
import com.chaoge.gymlog.domain.SetRow
import com.chaoge.gymlog.domain.TodayData
import com.chaoge.gymlog.domain.fmtWeight
import com.chaoge.gymlog.ui.components.GymLogSheet
import com.chaoge.gymlog.ui.components.NumberInputDialog
import com.chaoge.gymlog.ui.theme.DividerColor
import com.chaoge.gymlog.ui.theme.DoneColor
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.TextPrimary
import com.chaoge.gymlog.ui.theme.TextSecondary
import java.time.LocalDate

private sealed class Sheet {
    data class Switch(val item: ExerciseItem) : Sheet()
    data class EditAlt(val item: ExerciseItem) : Sheet()
    data class Add(val item: ExerciseItem) : Sheet()
    data class Detail(val item: ExerciseItem) : Sheet()
}

private sealed class NumEdit {
    data class Weight(val row: SetRow) : NumEdit()
    data class Reps(val row: SetRow) : NumEdit()
}

@Composable
fun TodayScreen(onFinished: (String) -> Unit) {
    val vm: TodayViewModel = viewModel()
    val data by vm.data.collectAsState()
    val days by vm.days.collectAsState()
    val restLeft by vm.restLeft.collectAsState()
    val loading by vm.loading.collectAsState()
    val alts by vm.alts.collectAsState()

    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var numEdit by remember { mutableStateOf<NumEdit?>(null) }
    var noteItem by remember { mutableStateOf<ExerciseItem?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (loading || data == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
            } else {
                TodayContent(
                    vm = vm,
                    days = days,
                    data = data!!,
                    onOpenSheet = { sheet = it },
                    onOpenNum = { numEdit = it },
                    onOpenNote = { noteItem = it },
                    onFinished = onFinished
                )
            }
        }

        restLeft?.let { RestBar(it, vm::addThirty, vm::skipRest) }

        when (val s = sheet) {
            is Sheet.Switch -> SwitchSheet(
                s.item, vm,
                onDismiss = { sheet = null },
                onOpenAdd = { sheet = Sheet.Add(s.item) },
                onOpenEditAlt = { vm.loadAlternatives(s.item.planExerciseId); sheet = Sheet.EditAlt(s.item) }
            )
            is Sheet.EditAlt -> EditAltSheet(
                s.item, alts, vm,
                onDismiss = { sheet = null },
                onOpenAdd = { sheet = Sheet.Add(s.item) }
            )
            is Sheet.Add -> AddSheet(s.item, vm) { sheet = null }
            is Sheet.Detail -> DetailSheet(s.item) { sheet = null }
            null -> {}
        }

        numEdit?.let { ne ->
            when (ne) {
                is NumEdit.Weight -> NumberInputDialog(
                    "重量 (kg)", fmtWeight(ne.row.weightKg), decimal = true,
                    onConfirm = { v -> v.toDoubleOrNull()?.let { vm.setWeight(ne.row, it) } },
                    onDismiss = { numEdit = null }
                )
                is NumEdit.Reps -> NumberInputDialog(
                    "次数", "${ne.row.reps}", decimal = false,
                    onConfirm = { v -> v.toIntOrNull()?.let { vm.setReps(ne.row, it) } },
                    onDismiss = { numEdit = null }
                )
            }
        }

        noteItem?.let { item ->
            NoteDialog(item, onSave = { vm.setNote(item, it) }, onDismiss = { noteItem = null })
        }
    }
}

@Composable
private fun TodayContent(
    vm: TodayViewModel,
    days: List<DayInfo>,
    data: TodayData,
    onOpenSheet: (Sheet) -> Unit,
    onOpenNum: (NumEdit) -> Unit,
    onOpenNote: (ExerciseItem) -> Unit,
    onFinished: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TodayHeader(days, data, onSwitchDay = vm::switchDay)
        LazyColumn(Modifier.weight(1f)) {
            items(data.items) { item ->
                ExerciseBlock(
                    item = item,
                    onSwitch = { onOpenSheet(Sheet.Switch(item)) },
                    onDetail = { onOpenSheet(Sheet.Detail(item)) },
                    onToggle = { row -> vm.toggleSet(item, row) },
                    onWeight = { row -> onOpenNum(NumEdit.Weight(row)) },
                    onReps = { row -> onOpenNum(NumEdit.Reps(row)) },
                    onNote = { onOpenNote(item) }
                )
            }
        }
        Box(Modifier.fillMaxWidth().background(PageBg).padding(12.dp, 12.dp, 12.dp, 8.dp)) {
            Button(
                onClick = { vm.finish(onFinished) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("完成今日训练", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun TodayHeader(days: List<DayInfo>, data: TodayData, onSwitchDay: (String) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val date = LocalDate.parse(data.dateKey)
    val week = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")[date.dayOfWeek.value % 7]
    val fraction = if (data.totalSets == 0) 0f else data.completedSets.toFloat() / data.totalSets

    Column(Modifier.fillMaxWidth().background(Color.White).padding(18.dp)) {
        Text("${date.monthValue}月${date.dayOfMonth}日 · $week", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Text("今天练", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Box {
            Row(
                Modifier.clickable { menuOpen = true }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(data.dayLabel, color = Primary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(6.dp))
                Text("▼", color = TextSecondary, fontSize = 15.sp)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                days.forEach { d ->
                    DropdownMenuItem(
                        text = { Text(d.label + "　" + d.tag) },
                        onClick = { onSwitchDay(d.key); menuOpen = false }
                    )
                }
            }
        }
        Text(data.dayTag, color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("训练进度：${data.completedSets} / ${data.totalSets}组", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(DividerColor)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Primary))
            }
            Spacer(Modifier.width(12.dp))
            Text("${data.elapsedSeconds / 60}分钟", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ExerciseBlock(
    item: ExerciseItem,
    onSwitch: () -> Unit,
    onDetail: () -> Unit,
    onToggle: (SetRow) -> Unit,
    onWeight: (SetRow) -> Unit,
    onReps: (SetRow) -> Unit,
    onNote: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(item.name, Modifier.clickable { onDetail() }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("⌄", Modifier.clickable { onSwitch() }.padding(4.dp), color = TextSecondary, fontSize = 18.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text("${item.sets}组 × ${item.minReps}–${item.maxReps}次 · RPE ${item.rpe}", color = TextSecondary, fontSize = 13.sp)
        item.lastLine?.let {
            Spacer(Modifier.height(3.dp))
            Text("上次：$it", color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("", Modifier.width(52.dp))
            Text("kg", Modifier.width(60.dp), color = TextSecondary, fontSize = 12.sp)
            Text("次数", Modifier.width(52.dp), color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("完成", color = TextSecondary, fontSize = 12.sp)
        }
        item.setRows.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("第${row.setIndex + 1}组", Modifier.width(52.dp), color = TextSecondary, fontSize = 13.sp)
                Text(
                    fmtWeight(row.weightKg) + "kg",
                    Modifier.width(60.dp).clickable { onWeight(row) },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("${row.reps}", Modifier.width(52.dp).clickable { onReps(row) }, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(34.dp).clip(CircleShape)
                        .background(if (row.isCompleted) DoneColor else Color.White)
                        .border(2.dp, if (row.isCompleted) DoneColor else DividerColor, CircleShape)
                        .clickable { onToggle(row) },
                    contentAlignment = Alignment.Center
                ) {
                    if (row.isCompleted) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    else Text("○", color = Color(0xFFC4C4C4))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("📝 备注 " + item.note.ifBlank { "添加备注" }, Modifier.clickable { onNote() }.padding(vertical = 6.dp), color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun RestBar(left: Int, onAdd30: () -> Unit, onSkip: () -> Unit) {
    val mm = left / 60
    val ss = left % 60
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), shape = RoundedCornerShape(14.dp), color = Color(0xFF171717)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("休息", color = Color(0xFFCCCCCC), fontSize = 13.sp)
                    Text(String.format("%02d:%02d", mm, ss), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onAdd30) { Text("＋30秒", color = Color.White) }
                TextButton(onClick = onSkip) { Text("跳过", color = Color.White) }
            }
        }
    }
}

@Composable
private fun SwitchSheet(
    item: ExerciseItem,
    vm: TodayViewModel,
    onDismiss: () -> Unit,
    onOpenAdd: () -> Unit,
    onOpenEditAlt: () -> Unit
) {
    GymLogSheet("切换动作", onDismiss) {
        item.candidates.forEachIndexed { idx, c ->
            val cur = c.exerciseId == item.exerciseId
            Row(
                Modifier.fillMaxWidth().clickable { vm.switchExercise(item, c.exerciseId); onDismiss() }.padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    (if (cur) "✓ " else "") + c.name + if (idx == 0) "　原计划" else "",
                    color = if (cur) Primary else TextPrimary,
                    fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        SheetAction("＋ 添加动作", onOpenAdd)
        SheetAction("编辑备选动作", onOpenEditAlt)
    }
}

@Composable
private fun EditAltSheet(
    item: ExerciseItem,
    alts: List<AltEntry>,
    vm: TodayViewModel,
    onDismiss: () -> Unit,
    onOpenAdd: () -> Unit
) {
    GymLogSheet("编辑备选动作", onDismiss) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.name, color = TextPrimary)
            Text("原计划 · 不可删除", color = TextSecondary, fontSize = 12.sp)
        }
        alts.forEach { a ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("≡ " + a.name, color = TextPrimary)
                Text("删除", Modifier.clickable { vm.deleteAlt(a.altId, item.planExerciseId) }.padding(4.dp), color = Primary, fontSize = 13.sp)
            }
        }
        SheetAction("＋ 添加动作", onOpenAdd)
    }
}

@Composable
private fun AddSheet(item: ExerciseItem, vm: TodayViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    GymLogSheet("添加动作", onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("例如：上斜杠铃卧推") }, singleLine = true)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { if (name.isNotBlank()) { vm.addAlternative(item, name.trim()); onDismiss() } },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("创建并切换") }
        }
    }
}

@Composable
private fun DetailSheet(item: ExerciseItem, onDismiss: () -> Unit) {
    GymLogSheet(item.name, onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("主要训练部位", color = TextSecondary, fontSize = 13.sp)
            Text(item.muscle.ifBlank { "未填写" }, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text("动作要点", color = TextSecondary, fontSize = 13.sp)
            Text(item.cues.ifBlank { "未填写" }, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun NoteDialog(item: ExerciseItem, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(item.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("当天备注") },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("本次训练备注") }) },
        confirmButton = { TextButton(onClick = { onSave(text); onDismiss() }) { Text("保存", color = Primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun SheetAction(text: String, onClick: () -> Unit) {
    Text(text, Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 15.dp), color = Primary, fontSize = 15.sp)
}
