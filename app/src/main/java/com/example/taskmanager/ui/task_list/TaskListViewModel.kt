package com.example.taskmanager.ui.task_list

import androidx.lifecycle.*
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import com.example.taskmanager.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    enum class SortType {
        DATE, PRIORITY, DUE_DATE
    }

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.DATE)

    // LiveData to navigate to task detail
    private val _navigateToTaskDetail = MutableLiveData<Long?>()
    val navigateToTaskDetail: LiveData<Long?> = _navigateToTaskDetail

    // LiveData to show delete confirmation
    private val _showDeleteConfirmation = MutableLiveData<Task?>()
    val showDeleteConfirmation: LiveData<Task?> = _showDeleteConfirmation

    // Combine search + sort to update task list dynamically
    val tasks: LiveData<List<Task>> = combine(_searchQuery, _sortType) { query, sort ->
        query to sort
    }.flatMapLatest { (query, sort) ->
        if (query.isEmpty()) {
            when (sort) {
                SortType.DATE -> repository.getTasksSortedByDate()
                SortType.PRIORITY -> repository.getTasksSortedByPriority()
                SortType.DUE_DATE -> repository.getTasksSortedByDueDate()
            }
        } else {
            repository.searchTasks(query)
        }
    }.asLiveData()

    fun searchTasks(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
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
}
