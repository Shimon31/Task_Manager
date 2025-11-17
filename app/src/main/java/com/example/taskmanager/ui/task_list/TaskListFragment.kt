package com.example.taskmanager.ui.task_list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.R
import com.example.taskmanager.TaskApplication
import com.example.taskmanager.data.model.Task
import com.example.taskmanager.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment() {

    private lateinit var binding: FragmentTaskListBinding

    private val viewModel: TaskListViewModel by viewModels {
        TaskListViewModelFactory((requireActivity().application as TaskApplication).repository)
    }

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        setupMenu()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                viewModel.onTaskClicked(task.id)
            },
            onDeleteClick = { task ->
                viewModel.onDeleteTaskClicked(task)
            }
        )

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }


    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskDetailFragment(-1)
            findNavController().navigate(action)
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_task_list, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.queryHint = "Search tasks..."

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchTasks(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)

            if (tasks.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.recyclerViewTasks.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.recyclerViewTasks.visibility = View.VISIBLE
            }
        }

        viewModel.navigateToTaskDetail.observe(viewLifecycleOwner) { taskId ->
            taskId?.let {
                val action = TaskListFragmentDirections
                    .actionTaskListFragmentToTaskDetailFragment(it)
                findNavController().navigate(action)
                viewModel.onNavigationComplete()
            }
        }

        viewModel.showDeleteConfirmation.observe(viewLifecycleOwner) { task ->
            task?.let { showDeleteConfirmationDialog(it) }
        }
    }

    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.onDeleteConfirmed(task)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.onDeleteCancelled()
            }
            .show()
    }
}

class TaskListViewModelFactory(
    private val repository: com.example.taskmanager.data.repository.TaskRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
