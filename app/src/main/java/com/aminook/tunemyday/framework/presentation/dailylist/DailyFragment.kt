package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.framework.presentation.common.ToDoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import com.aminook.tunemyday.util.TodoCallback
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_daily.*

@AndroidEntryPoint
class DailyFragment() : BaseFragment(R.layout.fragment_daily), ToDoAdapter.ToDoRecyclerViewListener,
    DailyScheduleAdapter.DailyScheduleAdapterListener {
    private val TAG="aminjoon"
    private var fragmentIndex:Int?=null
    private val dailyViewModel:DailyViewModel by viewModels()

    private lateinit var addTodoBtmSheetDialog:BottomSheetDialog


    private var subToDoAdapter:SubToDoAdapter?=null
    private var dailyScheduleAdapter:DailyScheduleAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            fragmentIndex = it.getInt(DAY_INDEX)
            fragmentIndex?.let {
                dailyViewModel.getDailySchedules(it)

            }

        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        top_toolbar_daily.title= dailyViewModel.getDay().fullName
        initializeAdapters()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        dailyViewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response,null)
            }
        }


    }

    private fun initializeAdapters() {
        dailyScheduleAdapter=DailyScheduleAdapter()
        dailyScheduleAdapter?.setListener(this)



        dailyViewModel.schedules.observe(viewLifecycleOwner){
            dailyScheduleAdapter?.submitList(it)


        }
        recycler_day_schedule.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=dailyScheduleAdapter
        }

    }



    override fun setSubTodoAdapter(itemView: ToDoAdapter.ViewHolder, todo: Todo) {
        subToDoAdapter= SubToDoAdapter()
        itemView.subTodoRecycler.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=subToDoAdapter
        }
    }

    override fun onEditTodoClick(todo: Todo, todoAdapter: ToDoAdapter) {
        showAddTodo(todo.scheduleId,todo.programId,todoAdapter,todo)
    }

    override fun onCheckChanged(todo: Todo, todoAdapter: ToDoAdapter) {
        dailyViewModel.updateTodo(todo).observeOnce(viewLifecycleOwner){
            todoAdapter.submitList(it)
        }
    }

    override fun swapItems(fromPosition: Int, toPosition: Int, toDoAdapter: ToDoAdapter) {
        val destTodo=toDoAdapter.currentList[toPosition].copy()
        val sourceTodo=toDoAdapter.currentList[fromPosition].copy()

        val destPriorityIndex=destTodo.priorityIndex
        val sourcePriorityIndex=sourceTodo.priorityIndex
        destTodo.priorityIndex=sourcePriorityIndex
        sourceTodo.priorityIndex=destPriorityIndex

        dailyViewModel.updateTodos(
            listOf(destTodo,sourceTodo),
            destTodo.scheduleId
        ).observeOnce(viewLifecycleOwner){

            toDoAdapter.submitList(it)
        }

    }

    override fun onDeleteTodoClick(todo: Todo, todoAdapter: ToDoAdapter) {
        dailyViewModel.deleteTodo(
            todo,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    dailyViewModel.addTodo(todo).observeOnce(viewLifecycleOwner) {
                        todoAdapter.submitList(it)
                    }
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
                }

            }
        ).observeOnce(viewLifecycleOwner){
            todoAdapter.submitList(it)
        }

    }


    override fun setTodoAdapter(holder: DailyScheduleAdapter.ViewHolder, schedule: Schedule) {
        val todoAdapter= ToDoAdapter()
        todoAdapter.setListener(this)
        Log.d(TAG, "setTodoAdapter: ${schedule.id}")
        dailyViewModel.getTodos(schedule.id).observeOnce(viewLifecycleOwner){
            todoAdapter.submitList(it)
        }
        val layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        val dividerItemDecoration=DividerItemDecoration(requireContext(),layoutManager.orientation)
        //todoAdapter.submitList(schedule.todos)
        holder.todoRecyclerView.apply {
            this.layoutManager=layoutManager
            adapter=todoAdapter
            addItemDecoration(dividerItemDecoration)
        }

        val callback= DragManageAdapter(
            todoAdapter,
            requireContext(),
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
           0
        )

        val helper=ItemTouchHelper(callback)

        helper.attachToRecyclerView(holder.todoRecyclerView)

    }



    override fun onAddNoteClick(scheduleId: Long,programId:Long, dailyScheduleAdapter: ToDoAdapter) {
        showAddTodo(scheduleId,programId,dailyScheduleAdapter)
    }

    private fun showAddTodo(scheduleId: Long,programId: Long, toDoAdapter: ToDoAdapter, todo:Todo?=null) {
        addTodoBtmSheetDialog= BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_add_todo,btn_sheet_add_todo)
        addTodoBtmSheetDialog.setContentView(view)
        addTodoBtmSheetDialog.show()
        view.txt_add_todo.requestFocus()
        todo?.let {
            view.txt_add_todo.setText(todo.title)
        }
        view.btn_save_todo.setOnClickListener {
            if(!view.txt_add_todo.text.isNullOrBlank()){
                val task=view.txt_add_todo.text
                if (todo==null) {
                    dailyViewModel.createTodo(scheduleId,programId, task.toString(),false)
                        .observeOnce(viewLifecycleOwner) {
                            toDoAdapter.submitList(it)
                        }
                }
                else{
                    dailyViewModel.updateTodo(todo.copy(title = task.toString())).observeOnce(viewLifecycleOwner){
                        toDoAdapter.submitList(it)
                        addTodoBtmSheetDialog.dismiss()
                    }
                }
                view.txt_add_todo.setText("")
            }else{
                //TODO(HANDLE BLANK
            }
        }
    }

    override fun onScheduleClick(scheduleId: Long) {

    }

    companion object {
        private const val DAY_INDEX = "param1"
        @JvmStatic
        fun newInstance(dayIndex: Int) =
            DailyFragment().apply {
                arguments = Bundle().apply {
                    putInt(DAY_INDEX, dayIndex)
                }
            }
    }

    override fun onDestroy() {
        subToDoAdapter=null
        dailyScheduleAdapter=null
        super.onDestroy()

    }
}