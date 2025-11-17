package com.example.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var title: String,
    var description: String? = null,
    var priority: Priority = Priority.LOW,
    var status: Status = Status.TODO,
    var dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)