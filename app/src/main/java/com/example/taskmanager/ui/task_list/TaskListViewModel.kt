// ui/task_list/TaskListViewModel.kt
package com.example.taskmanager.ui.task_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    enum class SortType {
        DATE, PRIORITY, DUE_DATE
    }

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.DATE)
    private val _filterPriority = MutableStateFlow<Priority?>(null)
    private val _filterStatus = MutableStateFlow<Status?>(null)

    val tasks: LiveData<List<Task>> = _searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            when (_sortType.value) {
                SortType.DATE -> repository.getTasksSortedByDate()
                SortType.PRIORITY -> repository.getTasksSortedByPriority()
                SortType.DUE_DATE -> repository.getTasksSortedByDueDate()
            }
        } else {
            repository.searchTasks(query)
        }
    }.asLiveData()

    private val _navigateToTaskDetail = MutableLiveData<Long?>()
    val navigateToTaskDetail: LiveData<Long?> = _navigateToTaskDetail

    private val _showDeleteConfirmation = MutableLiveData<Task?>()
    val showDeleteConfirmation: LiveData<Task?> = _showDeleteConfirmation

    fun searchTasks(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        _searchQuery.value = "" // Reset search to apply sort
    }

    fun setFilterPriority(priority: Priority?) {
        _filterPriority.value = priority
    }

    fun setFilterStatus(status: Status?) {
        _filterStatus.value = status
    }

    fun onTaskClicked(taskId: Long) {
        _navigateToTaskDetail.value = taskId
    }

    fun onNavigationComplete() {
        _navigateToTaskDetail.value = null
    }

    fun onDeleteTaskClicked(task: Task) {
        _showDeleteConfirmation.value = task
    }

    fun onDeleteConfirmed(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
        _showDeleteConfirmation.value = null
    }

    fun onDeleteCancelled() {
        _showDeleteConfirmation.value = null
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}