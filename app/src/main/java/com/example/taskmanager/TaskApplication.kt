package com.example.taskmanager

import android.app.Application
import com.example.taskmanager.data.db.AppDatabase
import com.example.taskmanager.data.repository.TaskRepository

class TaskApplication : Application() {

    // Create database instance
    val database by lazy { AppDatabase.getInstance(this) }

    // Create repository instance
    val repository by lazy { TaskRepository(database.taskDao()) }
}
