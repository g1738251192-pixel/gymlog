package com.chaoge.gymlog.data

import com.chaoge.gymlog.data.dao.ExerciseDao
import com.chaoge.gymlog.data.dao.PlanDao
import com.chaoge.gymlog.data.dao.PlanDayDao
import com.chaoge.gymlog.data.entity.Alternative
import com.chaoge.gymlog.data.entity.Exercise
import com.chaoge.gymlog.data.entity.PlanDay
import com.chaoge.gymlog.data.entity.PlanExercise

/**
 * 首次启动时的默认计划：推 / 拉 / 腿 各 5 个动作。
 */
private data class SeedExercise(
    val name: String,
    val muscle: String,
    val cues: String,
    val note: String,
    val alts: List<String>,
    val sets: Int,
    val minReps: Int,
    val maxReps: Int,
    val rpe: Int
)

private val PUSH = listOf(
    SeedExercise("杠铃平板卧推", "胸大肌 · 肱三头肌 · 三角肌前束", "肩胛骨收紧下沉，双脚踩实地面；杠铃下落至胸中部，控制离心；推起呼气。", "大重量请使用保护架或找人保护。", listOf("史密斯卧推", "哑铃卧推", "器械推胸"), 4, 8, 12, 8),
    SeedExercise("上斜哑铃卧推", "上胸 · 三角肌前束 · 肱三头肌", "凳面调至 30~45 度；下放至胸两侧。", "", listOf("上斜史密斯卧推", "上斜器械推胸"), 4, 8, 12, 8),
    SeedExercise("双杠臂屈伸", "下胸 · 肱三头肌", "身体略前倾练下胸，垂直练三头。", "", listOf("凳上臂屈伸", "窄距俯卧撑", "钢线下压"), 3, 8, 12, 8),
    SeedExercise("仰卧臂屈伸", "肱三头肌", "上臂固定不动，只动小臂。", "", listOf("钢线下压", "哑铃颈后臂屈伸"), 4, 10, 12, 8),
    SeedExercise("Y字侧平举", "三角肌中束", "小重量，控制节奏；手臂呈 Y 字抬起。", "", listOf("哑铃侧平举", "钢线侧平举"), 3, 12, 15, 8)
)

private val PULL = listOf(
    SeedExercise("单手钢线下拉", "背阔肌", "单臂下拉，肘贴身；顶峰收缩。", "", listOf("单手高位下拉", "对握下拉"), 4, 8, 12, 8),
    SeedExercise("对握高位下拉", "背阔肌 · 大圆肌", "对握把手，下拉至锁骨；避免耸肩。", "", listOf("高位下拉", "引体向上"), 4, 8, 12, 8),
    SeedExercise("单手器械划船", "背阔肌 · 菱形肌", "拉向髋部方向，肘贴身。", "", listOf("哑铃划船", "器械划船"), 4, 8, 12, 8),
    SeedExercise("开肘坐姿划船", "上背 · 三角肌后束", "肘向两侧打开，拉向胸中部。", "", listOf("坐姿划船", "俯身杠铃划船"), 4, 10, 12, 8),
    SeedExercise("钢线弯举", "肱二头肌", "肘固定，只屈肘；顶峰收缩。", "", listOf("杠铃弯举", "哑铃弯举"), 4, 10, 12, 8)
)

private val LEG = listOf(
    SeedExercise("单腿硬拉", "腘绳肌 · 臀大肌", "支撑腿微屈，髋部后移；背部平直。", "", listOf("哑铃单腿硬拉", "杠铃硬拉"), 4, 8, 12, 8),
    SeedExercise("保加利亚深蹲", "股四头肌 · 臀大肌", "后脚架高，前腿发力；膝盖对齐脚尖。", "", listOf("箭步蹲", "分腿蹲"), 4, 8, 10, 8),
    SeedExercise("颈前深蹲", "股四头肌 · 核心", "杠铃置于前肩，肘抬高；挺胸下蹲。", "", listOf("高杠深蹲", "哈克深蹲"), 4, 6, 8, 8),
    SeedExercise("罗马尼亚硬拉", "腘绳肌 · 臀大肌 · 竖脊肌", "髋部后移，杠铃贴腿下滑。", "", listOf("直腿硬拉", "杠铃硬拉"), 4, 8, 10, 8),
    SeedExercise("山羊挺身", "竖脊肌 · 臀大肌 · 腘绳肌", "髋部为轴，上半身起落。", "", listOf("反向挺身", "早安式"), 3, 12, 15, 8)
)

suspend fun seedIfNeeded(planDayDao: PlanDayDao, planDao: PlanDao, exerciseDao: ExerciseDao) {
    if (planDayDao.count() > 0) return

    planDayDao.insert(PlanDay("PUSH", "推日", "胸 · 肩 · 三头", 0))
    planDayDao.insert(PlanDay("PULL", "拉日", "背 · 二头", 1))
    planDayDao.insert(PlanDay("LEG", "腿日", "腿 · 臀", 2))

    suspend fun seedDay(dayKey: String, list: List<SeedExercise>) {
        list.forEachIndexed { index, se ->
            val mainId = ensureExercise(exerciseDao, se.name, se.muscle, se.cues, se.note)
            val peId = planDao.insert(
                PlanExercise(
                    dayType = dayKey, exerciseId = mainId, sortOrder = index,
                    sets = se.sets, minReps = se.minReps, maxReps = se.maxReps,
                    rpe = se.rpe, defaultRestSeconds = 90
                )
            )
            se.alts.forEachIndexed { ai, altName ->
                val altId = ensureExercise(exerciseDao, altName, "", "", "")
                planDao.insertAlt(
                    Alternative(planExerciseId = peId, exerciseId = altId, sortOrder = ai, isSystem = true, isHidden = false)
                )
            }
        }
    }

    seedDay("PUSH", PUSH)
    seedDay("PULL", PULL)
    seedDay("LEG", LEG)
}

private suspend fun ensureExercise(dao: ExerciseDao, name: String, muscle: String, cues: String, note: String): Long {
    dao.getByName(name)?.let { return it.id }
    return dao.insert(Exercise(name = name, muscle = muscle, cues = cues, note = note))
}
