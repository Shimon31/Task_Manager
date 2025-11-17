// ui/task_detail/TaskDetailFragment.kt
package com.example.taskmanager.ui.task_detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.R
import com.example.taskmanager.TaskApplication
import com.example.taskmanager.data.model.Priority
import com.example.taskmanager.data.model.Status
import com.example.taskmanager.databinding.FragmentTaskDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!


    private val viewModel: TaskDetailViewModel by viewModels {
        TaskDetailViewModelFactory((requireActivity().application as TaskApplication).repository)
    }

    private var selectedDueDate: Date? = null
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ... your existing setup calls ...
        setupSpinners()
        setupDatePicker()
        setupButtons()
        observeViewModel()

        // Read taskId from arguments (fallback to -1L for new task)
        val taskId = arguments?.getLong("taskId") ?: -1L
        viewModel.loadTask(taskId)
    }

    private fun setupSpinners() {
        // Priority Spinner
        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Priority.values().map { it.displayName }
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter

        // Status Spinner
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Status.values().map { it.displayName }
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
    }

    private fun setupDatePicker() {
        binding.btnSelectDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedDueDate?.let { calendar.time = it }

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDueDate = calendar.time
                    binding.tvDueDate.text = dateFormatter.format(selectedDueDate!!)
                    binding.btnClearDueDate.visibility = View.VISIBLE
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnClearDueDate.setOnClickListener {
            selectedDueDate = null
            binding.tvDueDate.text = "No due date selected"
            binding.btnClearDueDate.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveTask()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString().takeIf { it.isNotBlank() }
        val priority = Priority.values()[binding.spinnerPriority.selectedItemPosition]
        val status = Status.values()[binding.spinnerStatus.selectedItemPosition]

        viewModel.saveTask(title, description, priority, status, selectedDueDate)
    }

    private fun observeViewModel() {
        viewModel.task.observe(viewLifecycleOwner) { task ->
            if (task != null) {
                // Edit mode
                binding.etTitle.setText(task.title)
                binding.etDescription.setText(task.description ?: "")
                binding.spinnerPriority.setSelection(task.priority.ordinal)
                binding.spinnerStatus.setSelection(task.status.ordinal)

                selectedDueDate = task.dueDate as Date?
                if (task.dueDate != null) {
                    binding.tvDueDate.text = dateFormatter.format(task.dueDate)
                    binding.btnClearDueDate.visibility = View.VISIBLE
                } else {
                    binding.tvDueDate.text = "No due date selected"
                    binding.btnClearDueDate.visibility = View.GONE
                }

                binding.toolbar.title = "Edit Task"
            } else {
                // Create mode
                binding.toolbar.title = "New Task"
                binding.tvDueDate.text = "No due date selected"
                binding.btnClearDueDate.visibility = View.GONE
            }
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
                viewModel.onNavigationComplete()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onErrorShown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TaskDetailViewModelFactory(
    private val repository: com.example.taskmanager.data.repository.TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}