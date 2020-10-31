package com.aminook.tunemyday.framework.presentation.viewtodos

import android.animation.LayoutTransition
import android.os.Bundle
import android.util.Log
import android.view.View
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

@AndroidEntryPoint
class ViewTodoFragment : BaseFragment(R.layout.fragment_view_todo),
    TodoAdapter.ToDoRecyclerViewListener {

    private val TAG="aminjoon"

    private var unfinishedTodoAdapter:TodoAdapter?=null
    private var finishedTodoAdapter:TodoAdapter?=null
    private var isSummary=false

    private val viewModel: ViewTodoViewModel by viewModels()
    private lateinit var addTodoBtmSheetDialog: BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: ViewTodoFragmentArgs by navArgs()
        isSummary=args.isSummary
        initializeUnFinishedTodoAdapter()

        if (!isSummary) {
            black_line_separator.visibility=View.VISIBLE
            lbl_completed.visibility=View.VISIBLE
            recycler_view_todo_finished.visibility=View.VISIBLE
            initializeFinishedTodoAdapter()
        }else{
            black_line_separator.visibility=View.GONE
            lbl_completed.visibility=View.GONE
            recycler_view_todo_finished.visibility=View.GONE
            lbl_remaining.text="Tasks"
        }

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

        viewModel.scheduleLoaded.observe(viewLifecycleOwner){loaded->
            if (loaded){
                layout_nested_view_todo.visibility=View.VISIBLE
                layout_nested_view_todo.postDelayed({
                    layout_nested_view_todo.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                    layout_nested_view_todo.layoutTransition.setDuration(150)
                    layout_const_view_todo.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                    layout_const_view_todo.layoutTransition.setDuration(150)
                },100)

            }
        }


        if (scheduleId!=0L){
            viewModel.getSchedule(scheduleId,isSummary).observeOnce(viewLifecycleOwner){
                it?.let { schedule->
                    toolbar_view_todo.title=schedule.program.name
                    unfinishedTodoAdapter?.submitList(schedule.unfinishedTodos,
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
        }
        else{
            findNavController().popBackStack()
        }


        viewModel.newTodo.observe(viewLifecycleOwner){
           it?.let {
               unfinishedTodoAdapter?.addItem(it)
           }
        }

        viewModel.updatedTodo.observe(viewLifecycleOwner){
            it?.let {
                if (it.isDone){
                    finishedTodoAdapter?.updateItem(it)
                }else{
                    unfinishedTodoAdapter?.updateItem(it)
                }
            }
        }

        viewModel.deletedTodo.observe(viewLifecycleOwner){
          it?.let {
              if (it.isDone){
                  finishedTodoAdapter?.removeItem(it)
              }else{
                  unfinishedTodoAdapter?.removeItem(it)
              }
          }
        }

        viewModel.checkChangedTodo.observe(viewLifecycleOwner){
            it?.let {
                Log.d(TAG, "subscribeObservers: $it")
                if (it.isDone){
                    unfinishedTodoAdapter?.removeItem(it)
                    finishedTodoAdapter?.addItem(it)
                }else{
                    finishedTodoAdapter?.removeItem(it)
                    unfinishedTodoAdapter?.addItem(it)
                }
            }
        }

        viewModel.draggedTodos.observe(viewLifecycleOwner){
            it?.let {
                if (it.first().isDone){
                    finishedTodoAdapter?.moveItem(it)
                }else{
                    unfinishedTodoAdapter?.moveItem(it)
                }
            }
        }
    }

    private fun setupToolbar(){
        toolbar_view_todo.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initializeUnFinishedTodoAdapter() {
        val chLayoutManager= ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        unfinishedTodoAdapter= TodoAdapter(currentDay = viewModel.dateUtil.currentDayInInt,isSummary = isSummary)
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
        val chLayoutManager= ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        finishedTodoAdapter= TodoAdapter(currentDay = viewModel.dateUtil.currentDayInInt)
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

                    viewModel.changeTodoCheck(updatedTodo.copy(isDone = !checked),false)
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
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
            listOf(fromPosition,toPosition),
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
        if (todo!=null) {
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
                            Log.d(TAG, "execute: todo delete snackbar dismissed")
                        }

                    }
                )
                addTodoBtmSheetDialog.dismiss()
            }
        }else{
            view.btn_delete_todo.visibility=View.GONE
        }


        view.btn_save_todo.setOnClickListener {
            if (!view.txt_add_todo.text.isNullOrBlank()) {
                val task = view.txt_add_todo.text
                if (todo == null) {
                    viewModel.createTodo( task.toString())
                    addTodoBtmSheetDialog.dismiss()
                } else {
                    viewModel.updateTodo(
                        todo.copy(title = task.toString()),
                        true,
                        undoCallback = object : SnackbarUndoCallback {
                            override fun undo() {
                                viewModel.updateTodo(todo,false)
                            }
                        },
                        onDismissCallback = object : TodoCallback {
                            override fun execute() {
                                Log.d(TAG, "execute: todo delete snackbar dismissed")
                            }

                        }
                    )
                    addTodoBtmSheetDialog.dismiss()
                }
                view.txt_add_todo.setText("")
            } else {
                //TODO(HANDLE BLANK
            }
        }
    }





}