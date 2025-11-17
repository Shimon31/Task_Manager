package com.example.taskmanager.ui.task_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.util.Date

class TaskDetailViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadTask(taskId: Long) {
        if (taskId == -1L) {
            // New task
            _task.value = null
        } else {
            viewModelScope.launch {
                _task.value = repository.getTaskById(taskId)
            }
        }
    }

    fun saveTask(
        title: String,
        description: String?,
        priority: Priority,
        status: Status,
        dueDate: Date?
    ) {
        if (title.isBlank()) {
            _errorMessage.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch {
            val currentTask = _task.value
            val dueDateMillis = dueDate?.time  // ✅ Convert Date to Long

            if (currentTask == null) {
                // Create new task
                val newTask = Task(
                    title = title.trim(),
                    description = description?.trim(),
                    priority = priority,
                    status = status,
                    dueDate = dueDateMillis,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertTask(newTask)
            } else {
                // Update existing task
                val updatedTask = currentTask.copy(
                    title = title.trim(),
                    description = description?.trim(),
                    priority = priority,
                    status = status,
                    dueDate = dueDateMillis
                )
                repository.updateTask(updatedTask)
            }

            _navigateBack.value = true
        }
    }

    fun onNavigationComplete() {
        _navigateBack.value = false
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
}