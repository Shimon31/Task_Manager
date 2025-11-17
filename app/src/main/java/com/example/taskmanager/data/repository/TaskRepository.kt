package com.example.taskmanager.data.repository

import com.example.taskmanager.data.local.TaskDao
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)

    fun getTasksByPriority(priority: Priority): Flow<List<Task>> =
        taskDao.getTasksByPriority(priority)

    fun getTasksByStatus(status: Status): Flow<List<Task>> =
        taskDao.getTasksByStatus(status)

    fun getTasksSortedByDueDate(): Flow<List<Task>> = taskDao.getTasksSortedByDueDate()

    fun getTasksSortedByPriority(): Flow<List<Task>> = taskDao.getTasksSortedByPriority()

    fun getTasksSortedByDate(): Flow<List<Task>> = taskDao.getTasksSortedByDate()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteAllTasks() = taskDao.deleteAllTasks()
}