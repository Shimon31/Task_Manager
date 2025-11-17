// ui/task_list/TaskAdapter.kt
package com.example.taskmanager.ui.task_list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(task: Task) {
            binding.apply {
                // Set task title
                tvTaskTitle.text = task.title

                // Set task description
                tvTaskDescription.text = task.description ?: "No description"

                // Set priority
                tvPriority.text = task.priority.displayName
                tvPriority.setBackgroundColor(getPriorityColor(task.priority))

                // Set status
                tvStatus.text = task.status.displayName
                tvStatus.setBackgroundColor(getStatusColor(task.status))

                // Strike through if done
                if (task.status == Status.DONE) {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Set due date
                tvDueDate.text = if (task.dueDate != null) {
                    "Due: ${dateFormatter.format(task.dueDate)}"
                } else {
                    "No due date"
                }

                // Click listeners
                root.setOnClickListener { onTaskClick(task) }
                btnDelete.setOnClickListener { onDeleteClick(task) }
            }
        }

        private fun getPriorityColor(priority: Priority): Int {
            val colorRes = when (priority) {
                Priority.HIGH -> R.color.priority_high
                Priority.MEDIUM -> R.color.priority_medium
                Priority.LOW -> R.color.priority_low
            }
            return ContextCompat.getColor(binding.root.context, colorRes)
        }

        private fun getStatusColor(status: Status): Int {
            val colorRes = when (status) {
                Status.TODO -> R.color.status_todo
                Status.IN_PROGRESS -> R.color.status_in_progress
                Status.DONE -> R.color.status_done
            }
            return ContextCompat.getColor(binding.root.context, colorRes)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}