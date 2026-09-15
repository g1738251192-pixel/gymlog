package com.chaoge.gymlog

import android.app.Application
import com.chaoge.gymlog.data.AppDatabase
import com.chaoge.gymlog.data.Repository

class GymLogApplication : Application() {
    val database by lazy { AppDatabase.get(this) }
    val repository by lazy { Repository(database) }
}
