package com.chaoge.gymlog.ui.navigation

/** 简单导航：底部三页 + 详情子页 */
sealed class Screen {
    object Today : Screen()
    object Plan : Screen()
    object History : Screen()
    data class Record(val dateKey: String) : Screen()
    data class ExerciseHistory(val exerciseId: Long, val name: String) : Screen()
    data class Done(val dateKey: String) : Screen()
}
