package com.chaoge.gymlog.ui.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chaoge.gymlog.domain.PlanItem
import com.chaoge.gymlog.ui.components.GymLogSheet
import com.chaoge.gymlog.ui.theme.DangerColor
import com.chaoge.gymlog.ui.theme.DividerColor
import com.chaoge.gymlog.ui.theme.PageBg
import com.chaoge.gymlog.ui.theme.Primary
import com.chaoge.gymlog.ui.theme.PrimaryBg
import com.chaoge.gymlog.ui.theme.TextSecondary

@Composable
fun PlanScreen() {
    val vm: PlanViewModel = viewModel()
    val days by vm.days.collectAsState()
    val day by vm.day.collectAsState()
    val items by vm.items.collectAsState()

    var editingItem by remember { mutableStateOf<PlanItem?>(null) }
    var showAddEx by remember { mutableStateOf(false) }
    var showNewDay by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf<PlanItem?>(null) }
    var confirmDeleteDay by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(PageBg)) {
        // 标题 + 新建/删除日
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(days.firstOrNull { it.key == day }?.label ?: "", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Text("＋新建训练日", color = Primary, fontSize = 13.sp, modifier = Modifier.clickable { showNewDay = true }.padding(4.dp))
            Spacer(Modifier.width(10.dp))
            Text("删除此日", color = DangerColor, fontSize = 13.sp, modifier = Modifier.clickable { if (days.size > 1) confirmDeleteDay = true }.padding(4.dp))
        }
        // 训练日切换
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(10.dp)).background(DividerColor).padding(3.dp)) {
            days.forEach { d ->
                val selected = d.key == day
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color.White else Color.Transparent)
                        .clickable { vm.selectDay(d.key) }.padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(d.label.replace("日", ""), color = if (selected) Primary else TextSecondary, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        // 动作列表
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(items) { idx, item ->
                PlanRow(idx, item, onEdit = { editingItem = item }, onDelete = { deleteItem = item })
            }
        }
        // 添加动作
        Button(
            onClick = { showAddEx = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp).height(46.dp)
        ) { Text("＋ 添加动作", color = Primary, fontWeight = FontWeight.Bold) }
        Text("计划改完自动同步到「今日」", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
    }

    editingItem?.let { item ->
        PlanEditSheet(item, onSave = { name, muscle, sets, min, max, rpe, rest, alts ->
            vm.saveExercise(item.planExerciseId, name, muscle, sets, min, max, rpe, rest, alts)
            editingItem = null
        }, onDismiss = { editingItem = null })
    }
    if (showAddEx) {
        AddExerciseDialog(onConfirm = { vm.addExercise(it); showAddEx = false }, onDismiss = { showAddEx = false })
    }
    if (showNewDay) {
        NewDayDialog(onConfirm = { vm.createDay(it); showNewDay = false }, onDismiss = { showNewDay = false })
    }
    deleteItem?.let { item ->
        ConfirmDialog("删除动作", "从计划里删除「${item.name}」？", onConfirm = { vm.deleteExercise(item.planExerciseId); deleteItem = null }, onDismiss = { deleteItem = null })
    }
    if (confirmDeleteDay) {
        ConfirmDialog("删除训练日", "删除「${days.firstOrNull { it.key == day }?.label}」整套计划？", onConfirm = { vm.deleteDay(); confirmDeleteDay = false }, onDismiss = { confirmDeleteDay = false })
    }
}

@Composable
private fun PlanRow(index: Int, item: PlanItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(PrimaryBg), contentAlignment = Alignment.Center) {
            Text("${index + 1}", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("${item.sets}组 × ${item.minReps}–${item.maxReps}次 · RPE ${item.rpe} · 休息${item.restSeconds}秒", color = TextSecondary, fontSize = 12.sp)
        }
        Text("编辑", color = Primary, fontSize = 13.sp, modifier = Modifier.clickable { onEdit() }.padding(4.dp))
        Text("删除", color = DangerColor, fontSize = 13.sp, modifier = Modifier.clickable { onDelete() }.padding(4.dp))
    }
    HorizontalDivider(color = DividerColor)
}

@Composable
private fun PlanEditSheet(
    item: PlanItem,
    onSave: (String, String, Int, Int, Int, Int, Int, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var muscle by remember { mutableStateOf(item.muscle) }
    var sets by remember { mutableStateOf(item.sets.toString()) }
    var min by remember { mutableStateOf(item.minReps.toString()) }
    var max by remember { mutableStateOf(item.maxReps.toString()) }
    var rpe by remember { mutableStateOf(item.rpe.toString()) }
    var rest by remember { mutableStateOf(item.restSeconds.toString()) }
    var alts by remember { mutableStateOf(item.alternatives.joinToString(", ")) }

    GymLogSheet("编辑动作", onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            LabeledField("动作名称") { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true) }
            LabeledField("主要部位（可选）") { OutlinedTextField(muscle, { muscle = it }, Modifier.fillMaxWidth(), singleLine = true) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("组数", Modifier.weight(1f)) { NumField(sets) { sets = it } }
                LabeledField("次数下限", Modifier.weight(1f)) { NumField(min) { min = it } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("次数上限", Modifier.weight(1f)) { NumField(max) { max = it } }
                LabeledField("RPE", Modifier.weight(1f)) { NumField(rpe) { rpe = it } }
            }
            LabeledField("休息时间（秒）") { NumField(rest) { rest = it } }
            LabeledField("备选动作（逗号分隔）") { OutlinedTextField(alts, { alts = it }, Modifier.fillMaxWidth(), placeholder = { Text("史密斯卧推, 哑铃卧推") }) }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        name.trim(), muscle.trim(),
                        sets.toIntOrNull() ?: item.sets,
                        min.toIntOrNull() ?: item.minReps,
                        max.toIntOrNull() ?: item.maxReps,
                        rpe.toIntOrNull() ?: item.rpe,
                        rest.toIntOrNull() ?: item.restSeconds,
                        alts.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun NumField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, onValueChange, Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable
private fun LabeledField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
        content()
    }
}

@Composable
private fun AddExerciseDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加动作") },
        text = { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), placeholder = { Text("动作名称") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onConfirm(name.trim()); onDismiss() } }, enabled = name.isNotBlank()) { Text("添加", color = Primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun NewDayDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建训练日") },
        text = { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), placeholder = { Text("例如：肩日 / 有氧日") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) { onConfirm(name.trim()); onDismiss() } }, enabled = name.isNotBlank()) { Text("创建", color = Primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun ConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text("删除", color = DangerColor) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
