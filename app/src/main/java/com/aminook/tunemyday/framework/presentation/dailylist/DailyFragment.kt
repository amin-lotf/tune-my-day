package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.*
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_daily.*

@AndroidEntryPoint
class DailyFragment : BaseFragment(R.layout.fragment_daily),
    DailyScheduleAdapter.DailyScheduleAdapterListener {
   // private val TAG = "aminjoon"
    private val dailyViewModel: DailyViewModel by viewModels()
    private  var dailyScheduleAdapter:DailyScheduleAdapter?=null
    private  var addTodoBtmSheetDialog: BottomSheetDialog?=null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dailyViewModel.getRoutineIndex().observeOnce(viewLifecycleOwner) {
            if (it != 0L) {
                dailyViewModel.getDailySchedules(it)
            }
        }
        initializeAdapters()

        subscribeObservers()
        top_toolbar_daily.title = "Today"
        dailyViewModel.getScreenType().observe(viewLifecycleOwner){screenType->
            when(screenType){
                SCREEN_WEEKLY ->{
                    findNavController().navigateWithSourcePopUp(R.id.dailyFragment,R.id.weeklyListFragment)
                }
                SCREEN_BLANK ->{
                    findNavController().navigateWithSourcePopUp(R.id.dailyFragment,R.id.noDataFragment)
                }
            }
        }

        layout_refresh_daily.setOnRefreshListener {
            dailyViewModel.getRoutineIndex().observeOnce(viewLifecycleOwner) {
                if (it != 0L) {
                    dailyViewModel.getDailySchedules(it)
                    layout_refresh_daily.isRefreshing=false
                }
            }
        }
    }




    override fun onResume() {
        super.onResume()
        if (dailyScheduleAdapter==null){
            initializeAdapters()
            subscribeObservers()
        }
        dailyScheduleAdapter?.setListener(this)
    }


    private fun subscribeObservers() {
        dailyViewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }
        }

        dailyViewModel.schedules.observe(viewLifecycleOwner) {
            if (it.isEmpty()){
                txt_no_schedule_daily.visibility=View.VISIBLE
            }else{
                txt_no_schedule_daily.visibility=View.GONE
            }

            dailyScheduleAdapter?.submitList(it)
            layout_nested_daily.visibility=View.VISIBLE
        }
    }

    private fun initializeAdapters() {

        val chLayoutManager=ChipsLayoutManager.newBuilder(requireContext())
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .withLastRow(true)
            .build()

           dailyScheduleAdapter = DailyScheduleAdapter(
               requireContext(),
               dailyViewModel.dateUtil.curDayIndex,
               dailyViewModel.dateUtil.currentDayInInt
           )

        recycler_day_schedule.apply {
            layoutManager =
                chLayoutManager
            adapter = dailyScheduleAdapter
            isNestedScrollingEnabled=false
        }
    }

    override fun onEditTodoClick(todo: Todo,position: Int, todoAdapter: TodoAdapter?) {
        showAddTodo(todo.scheduleId, todo.programId, todoAdapter, todo,position)
    }

    override fun onCheckChanged(todo: Todo, checked: Boolean,position: Int, todoAdapter: TodoAdapter?) {
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
                override fun execute() {}
            }
            ).observe(viewLifecycleOwner){
            it?.let {
                todoAdapter?.removeItem(it)
            }
        }
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
            BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
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
        view.txt_add_todo.setOnEditorActionListener { _, actionId, event ->
            var handled=false
                if (actionId==EditorInfo.IME_ACTION_GO){
                    view.btn_save_todo.performClick()
                    handled=true
                }
            return@setOnEditorActionListener handled
        }
        view.txt_add_todo.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrBlank() && view.txt_add_todo_input_layout.error!=null){
                view.txt_add_todo_input_layout.error=null
            }
        }

        view.btn_save_todo.setOnClickListener {
            val task = view.txt_add_todo.text.toString()
            if (task.isNotBlank()) {

                if (todo == null) {
                    dailyViewModel.createTodo(scheduleId, programId, task, false)
                        .observeOnce(viewLifecycleOwner) {
                            it?.let {
                                todoAdapter?.addItem(it)
                                addTodoBtmSheetDialog?.dismiss()
                            }

                        }
                } else {
                    dailyViewModel.updateTodo(
                        todo.copy(title = task),
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
                view.txt_add_todo_input_layout.error="Invalid Name"
            }
        }
    }

    override fun onScheduleClick(scheduleId: Long) {
        val action=DailyFragmentDirections.actionDailyFragmentToViewTodoFragment(scheduleId)
        findNavController().navigate(action)

    }

    override fun onTimeClick(action: NavDirections) {
        findNavController().navigate(action)
    }

    override fun onPause() {
        dailyScheduleAdapter?.setListener(null)
        addTodoBtmSheetDialog=null
        super.onPause()

    }
    override fun onDestroy() {
        dailyScheduleAdapter=null
        super.onDestroy()
    }

}