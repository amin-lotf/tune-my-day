package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.framework.datasource.cache.database.TodoDao
import com.aminook.tunemyday.framework.datasource.cache.mappers.DetailedScheduleCacheMapper
import com.aminook.tunemyday.framework.datasource.cache.mappers.TodoCacheMapper
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.TodoCallback
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_daily.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DailyFragment : BaseFragment(R.layout.fragment_daily),
    DailyScheduleAdapter.DailyScheduleAdapterListener {
    private val TAG = "aminjoon"
    private var fragmentIndex: Int? = null
    private val dailyViewModel: DailyViewModel by viewModels()

    private  var addTodoBtmSheetDialog: BottomSheetDialog?=null

    @Inject
    lateinit var todoDao: TodoDao

    @Inject
    lateinit var todoCacheMapper: TodoCacheMapper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyViewModel.getRoutineIndex().observeOnce(viewLifecycleOwner) {
            if (it != 0L) {
                dailyViewModel.getDailySchedules(it)
            }
        }
        top_toolbar_daily.title = "Today"
        Log.d(TAG, "onViewCreated: ")
        initializeAdapters()
        subscribeObservers()


    }


    private fun subscribeObservers() {
        dailyViewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }

        }



    }

    private fun initializeAdapters() {
        val dailyScheduleAdapter = DailyScheduleAdapter(
            requireContext(),
            dailyViewModel.dateUtil.curDayIndex,
            dailyViewModel.dateUtil.currentDayInInt
        )
        dailyScheduleAdapter.setListener(this)


        dailyViewModel.schedules.observeOnce(viewLifecycleOwner) {
            Log.d(TAG, "initializeAdapters: observe once")
            dailyScheduleAdapter.submitList(it)


        }
        (recycler_day_schedule.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_day_schedule.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = dailyScheduleAdapter
            isNestedScrollingEnabled = false
        }

    }


    override fun onEditTodoClick(todo: Todo,position: Int, todoAdapter: TodoAdapter?) {
        showAddTodo(todo.scheduleId, todo.programId, todoAdapter, todo,position)
    }

    override fun onCheckChanged(todo: Todo, checked: Boolean,position: Int, todoAdapter: TodoAdapter?) {

        Log.d(TAG, "onCheckChanged: ${todo.title} pos:$position")
        val updatedTodo = Todo(
            id = todo.id,
            title = todo.title,
            scheduleId = todo.scheduleId,
            programId = todo.programId,
            isDone = checked,
            dateAdded = todo.dateAdded,
            priorityIndex = todo.priorityIndex,
            lastChecked = dailyViewModel.dateUtil.currentDayInInt
        )
        dailyViewModel.changeTodoCheck(
            updatedTodo,
            true,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    dailyViewModel.changeTodoCheck(todo.copy(),false).observeOnce(viewLifecycleOwner) {
                        it?.let {
                            todoAdapter?.addItem(it)

                        }
                    }
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
                }

            }
            ).observe(viewLifecycleOwner){
            it?.let {
                todoAdapter?.removeItem(it)
            }
        }
    }

    override fun swipeToDelete(todo: Todo, position: Int, todoAdapter: TodoAdapter?) {

    }

    override fun updateTodos(todos: List<Todo>) {
        dailyViewModel.moveTodos(todos,0).observe(viewLifecycleOwner){}
    }

    override fun swapItems(fromPosition: Todo, toPosition: Todo, todoAdapter: TodoAdapter?) {
        todoAdapter?.let {

            val destPriorityIndex = toPosition.priorityIndex
            val sourcePriorityIndex = fromPosition.priorityIndex
            toPosition.priorityIndex = sourcePriorityIndex
            fromPosition.priorityIndex = destPriorityIndex

            dailyViewModel.moveTodos(
                listOf(fromPosition,toPosition),
                toPosition.scheduleId
            ).observeOnce(viewLifecycleOwner){
                if (it.isNotEmpty()){
                    todoAdapter.moveItem(it)
                }

            }
        }


    }


    override fun onAddNoteClick(
        scheduleId: Long,
        programId: Long,
        dailyScheduleAdapter: TodoAdapter?
    ) {
        showAddTodo(scheduleId, programId, dailyScheduleAdapter)
    }

    private fun showAddTodo(
        scheduleId: Long,
        programId: Long,
        todoAdapter: TodoAdapter?,
        todo: Todo? = null,
        position: Int=0
    ) {
        addTodoBtmSheetDialog =
            BottomSheetDialog(requireContext(), R.style.ThemeOverlay_DialogStyle)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_todo, btn_sheet_add_todo)
        addTodoBtmSheetDialog?.setContentView(view)
        addTodoBtmSheetDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        addTodoBtmSheetDialog?.show()
        todo?.let {
            view.txt_add_todo.setText(todo.title)
        }
        view.txt_add_todo.requestFocus()
        if (todo!=null) {
            view.btn_delete_todo.setOnClickListener {
                dailyViewModel.deleteTodo(
                    todo,
                    undoCallback = object : SnackbarUndoCallback {
                        override fun undo() {
                            dailyViewModel.addTodo(todo).observeOnce(viewLifecycleOwner) {
                                it?.let {
                                    todoAdapter?.addItem(it)

                                }
                            }
                        }

                    },
                    onDismissCallback = object : TodoCallback {
                        override fun execute() {
                            Log.d(TAG, "execute: todo delete snackbar dismissed")
                        }

                    }
                ).observe(viewLifecycleOwner){
                    it?.let {
                        todoAdapter?.removeItem(it)
                        addTodoBtmSheetDialog?.dismiss()
                    }
                }
            }
        }else{
            view.btn_delete_todo.visibility=View.GONE
        }


        view.btn_save_todo.setOnClickListener {
            if (!view.txt_add_todo.text.isNullOrBlank()) {
                val task = view.txt_add_todo.text
                if (todo == null) {
                    dailyViewModel.createTodo(scheduleId, programId, task.toString(), false)
                        .observeOnce(viewLifecycleOwner) {
                            it?.let {
                                todoAdapter?.addItem(it)
                                addTodoBtmSheetDialog?.dismiss()
                            }

                        }
                } else {
                    dailyViewModel.updateTodo(
                        todo.copy(title = task.toString()),
                        true,
                        undoCallback = object : SnackbarUndoCallback {
                            override fun undo() {
                                dailyViewModel.updateTodo(todo,false).observeOnce(viewLifecycleOwner) {
                                    it?.let {
                                        todoAdapter?.updateItem(todo)

                                    }
                                }
                            }

                        },
                        onDismissCallback = object : TodoCallback {
                            override fun execute() {
                                Log.d(TAG, "execute: todo delete snackbar dismissed")
                            }

                        }
                    )
                        .observeOnce(viewLifecycleOwner) {
                            it?.let {
                                todoAdapter?.updateItem(it)
                            }

                            addTodoBtmSheetDialog?.dismiss()
                        }
                }
                view.txt_add_todo.setText("")
            } else {
                //TODO(HANDLE BLANK
            }
        }
    }

    override fun onScheduleClick(scheduleId: Long) {

        val action=DailyFragmentDirections.actionDailyFragmentToViewTodoFragment(scheduleId)
        findNavController().navigate(action)

    }

    override fun onDestroy() {
        addTodoBtmSheetDialog= null
        super.onDestroy()
    }

}