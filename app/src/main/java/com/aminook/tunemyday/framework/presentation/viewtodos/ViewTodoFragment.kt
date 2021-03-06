package com.aminook.tunemyday.framework.presentation.viewtodos

import android.animation.LayoutTransition
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback

import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import com.aminook.tunemyday.util.TodoCallback
import com.aminook.tunemyday.util.observeOnce
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_view_todo.*
import java.util.*
import kotlin.concurrent.schedule

@AndroidEntryPoint
class ViewTodoFragment : BaseFragment(R.layout.fragment_view_todo),
    TodoAdapter.ToDoRecyclerViewListener {

    //private val TAG="aminjoon"

    private var unfinishedTodoAdapter: TodoAdapter? = null
    private var finishedTodoAdapter: TodoAdapter? = null
    private var isSummary = false
    private val viewModel: ViewTodoViewModel by viewModels()
    private lateinit var addTodoBtmSheetDialog: BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ViewTodoFragmentArgs by navArgs()
        isSummary = args.isSummary
        if (!isSummary) {
            black_line_separator.visibility = View.VISIBLE
            lbl_completed.visibility = View.VISIBLE
            recycler_view_todo_finished.visibility = View.VISIBLE
            initializeFinishedTodoAdapter()
        } else {
            black_line_separator.visibility = View.GONE
            lbl_completed.visibility = View.GONE
            recycler_view_todo_finished.visibility = View.GONE
            lbl_remaining.text = "Tasks"
        }
        initializeUnFinishedTodoAdapter()
        setupToolbar()
        subscribeObservers(args.scheduleId)
    }

    override fun onResume() {
        super.onResume()
        setupFab()
    }

    private fun setupFab() {
        requireActivity().fab_schedule.setOnClickListener {
            showAddTodo()
        }
    }

    private fun subscribeObservers(scheduleId: Long) {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }
        }
        viewModel.scheduleLoaded.observe(viewLifecycleOwner) { loaded ->
            if (loaded) {
                layout_parent_view_todo.visibility = View.VISIBLE
                Timer("delayedAnimation", false).schedule(500L) {
                    layout_nested_view_todo.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                    layout_nested_view_todo.layoutTransition.setDuration(150)
                    layout_const_view_todo.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                    layout_const_view_todo.layoutTransition.setDuration(150)
                }
            }
        }
        if (scheduleId != 0L) {
            viewModel.getSchedule(scheduleId, isSummary).observeOnce(viewLifecycleOwner) {
                it?.let { schedule ->
                    toolbar_view_todo.title = schedule.program.name
                    unfinishedTodoAdapter?.submitList(
                        schedule.unfinishedTodos,
                        addPadding = false,
                        withAddButton = false
                    )
                    if (!isSummary) {
                        finishedTodoAdapter?.submitList(
                            schedule.finishedTodos,
                            addPadding = false,
                            withAddButton = false
                        )
                    }
                }
            }
        } else {
            findNavController().popBackStack()
        }
        viewModel.newTodo.observe(viewLifecycleOwner) {
            it?.let {
                unfinishedTodoAdapter?.addItem(it)
            }
        }
        viewModel.updatedTodo.observe(viewLifecycleOwner) {
            it?.let {
                if (isSummary) {
                    unfinishedTodoAdapter?.updateItem(it)
                } else {
                    if (it.isDone) {
                        finishedTodoAdapter?.updateItem(it)
                    } else {
                        unfinishedTodoAdapter?.updateItem(it)
                    }
                }
            }
        }
        viewModel.deletedTodo.observe(viewLifecycleOwner) {
            it?.let {
                if (isSummary) {
                    unfinishedTodoAdapter?.removeItem(it)
                } else {
                    if (it.isDone) {
                        finishedTodoAdapter?.removeItem(it)
                    } else {
                        unfinishedTodoAdapter?.removeItem(it)
                    }
                }
            }
        }
        viewModel.checkChangedTodo.observe(viewLifecycleOwner) {
            it?.let {
                if (isSummary) {
                    unfinishedTodoAdapter?.removeItem(it)
                } else {
                    if (it.isDone) {
                        unfinishedTodoAdapter?.removeItem(it)
                        finishedTodoAdapter?.addItem(it)
                    } else {
                        finishedTodoAdapter?.removeItem(it)
                        unfinishedTodoAdapter?.addItem(it)
                    }
                }
            }
        }

        viewModel.draggedTodos.observe(viewLifecycleOwner) {
            it?.let {
                if (isSummary) {
                    unfinishedTodoAdapter?.moveItem(it)
                } else {
                    if (it.first().isDone) {
                        finishedTodoAdapter?.moveItem(it)
                    } else {
                        unfinishedTodoAdapter?.moveItem(it)
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        toolbar_view_todo.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initializeUnFinishedTodoAdapter() {
        val chLayoutManager = ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        unfinishedTodoAdapter =
            TodoAdapter(currentDay = viewModel.dateUtil.currentDayInInt, isSummary = isSummary)
        unfinishedTodoAdapter?.setListener(this)
        unfinishedTodoAdapter?.let {
            recycler_view_todo_unfinished.apply {
                this.layoutManager = chLayoutManager
                adapter = unfinishedTodoAdapter
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun initializeFinishedTodoAdapter() {
        val chLayoutManager = ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        finishedTodoAdapter = TodoAdapter(currentDay = viewModel.dateUtil.currentDayInInt)
        finishedTodoAdapter?.setListener(this)
        finishedTodoAdapter?.let {
            recycler_view_todo_finished.apply {
                this.layoutManager = chLayoutManager
                adapter = finishedTodoAdapter
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onEditTodoClick(todo: Todo, position: Int) {
        showAddTodo(todo)
    }

    override fun onCheckChanged(todo: Todo, checked: Boolean, position: Int) {
        val updatedTodo = Todo(
            id = todo.id,
            title = todo.title,
            scheduleId = todo.scheduleId,
            programId = todo.programId,
            isDone = checked,
            dateAdded = todo.dateAdded,
            priorityIndex = todo.priorityIndex,
            lastChecked = viewModel.dateUtil.currentDayInInt,
            index = position
        )
        viewModel.changeTodoCheck(
            updatedTodo,
            true,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    viewModel.changeTodoCheck(updatedTodo.copy(isDone = !checked), false)
                }
            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                }

            }
        )
    }

    override fun swapItems(fromPosition: Todo, toPosition: Todo) {
        val destPriorityIndex = toPosition.priorityIndex
        val sourcePriorityIndex = fromPosition.priorityIndex
        toPosition.priorityIndex = sourcePriorityIndex
        fromPosition.priorityIndex = destPriorityIndex
        viewModel.moveTodos(
            listOf(fromPosition, toPosition),
            scheduleId = toPosition.scheduleId
        )
    }

    private fun showAddTodo(
        todo: Todo? = null
    ) {
        addTodoBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_todo, btn_sheet_add_todo)
        addTodoBtmSheetDialog.setContentView(view)
        addTodoBtmSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        addTodoBtmSheetDialog.show()
        todo?.let {
            view.txt_add_todo.setText(todo.title)
        }
        view.txt_add_todo.requestFocus()
        if (todo != null) {
            view.btn_delete_todo.setOnClickListener {
                viewModel.deleteTodo(
                    todo,
                    undoCallback = object : SnackbarUndoCallback {
                        override fun undo() {
                            viewModel.addTodo(todo)
                        }
                    },
                    onDismissCallback = object : TodoCallback {
                        override fun execute() {
                        }
                    }
                )
                addTodoBtmSheetDialog.dismiss()
            }
        } else {
            view.btn_delete_todo.visibility = View.GONE
        }
        view.txt_add_todo.setOnEditorActionListener { _, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_GO) {
                view.btn_save_todo.performClick()
                handled = true
            }
            return@setOnEditorActionListener handled
        }
        view.txt_add_todo.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrBlank() && view.txt_add_todo_input_layout.error != null) {
                view.txt_add_todo_input_layout.error = null
            }
        }
        view.btn_save_todo.setOnClickListener {
            val task = view.txt_add_todo.text.toString().trim()
            if (!task.isBlank()) {
                if (todo == null) {
                    viewModel.createTodo(task)
                    addTodoBtmSheetDialog.dismiss()
                } else {
                    viewModel.updateTodo(
                        todo.copy(title = task),
                        true,
                        undoCallback = object : SnackbarUndoCallback {
                            override fun undo() {
                                viewModel.updateTodo(todo, false)
                            }
                        },
                        onDismissCallback = object : TodoCallback {
                            override fun execute() {
                            }

                        }
                    )
                    addTodoBtmSheetDialog.dismiss()
                }
                view.txt_add_todo.setText("")
            } else {
                view.txt_add_todo_input_layout.error = "Invalid Name"
            }
        }
    }
}