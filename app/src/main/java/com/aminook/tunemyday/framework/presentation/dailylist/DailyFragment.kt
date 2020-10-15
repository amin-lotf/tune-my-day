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
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import com.aminook.tunemyday.util.TodoCallback
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_daily.*

@AndroidEntryPoint
class DailyFragment : BaseFragment(R.layout.fragment_daily),
    DailyScheduleAdapter.DailyScheduleAdapterListener {
    private val TAG="aminjoon"
    private var fragmentIndex:Int?=null
    private val dailyViewModel:DailyViewModel by viewModels()

    private lateinit var addTodoBtmSheetDialog:BottomSheetDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyViewModel.getRoutineIndex().observeOnce(viewLifecycleOwner){
            if(it!=0L) {
                dailyViewModel.getDailySchedules(it)
            }
        }
        top_toolbar_daily.title= "Today"
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
        val dailyScheduleAdapter=DailyScheduleAdapter(requireContext())
        dailyScheduleAdapter.setListener(this)


        dailyViewModel.schedules.observeOnce(viewLifecycleOwner){
            Log.d(TAG, "initializeAdapters: ")
            dailyScheduleAdapter.submitList(it)


        }
        recycler_day_schedule.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=dailyScheduleAdapter
        }

    }



    override fun onEditTodoClick(todo: Todo, todoAdapter: TodoAdapter?) {
        showAddTodo(todo.scheduleId,todo.programId,todoAdapter,todo)
    }

    override fun onCheckChanged(todo: Todo, todoAdapter: TodoAdapter?) {
        dailyViewModel.updateTodo(todo).observeOnce(viewLifecycleOwner){
            todoAdapter?.submitList(it)
        }
    }

    override fun swapItems(fromPosition: Int, toPosition: Int, todoAdapter: TodoAdapter?) {
        todoAdapter?.let {
            val destTodo=todoAdapter.currentList[toPosition].copy()
            val sourceTodo=todoAdapter.currentList[fromPosition].copy()

            val destPriorityIndex=destTodo.priorityIndex
            val sourcePriorityIndex=sourceTodo.priorityIndex
            destTodo.priorityIndex=sourcePriorityIndex
            sourceTodo.priorityIndex=destPriorityIndex

            dailyViewModel.updateTodos(
                listOf(destTodo,sourceTodo),
                destTodo.scheduleId
            ).observeOnce(viewLifecycleOwner){

                todoAdapter.submitList(it)
            }
        }


    }

    override fun onDeleteTodoClick(todo: Todo, todoAdapter: TodoAdapter?) {
        dailyViewModel.deleteTodo(
            todo,
            undoCallback = object : SnackbarUndoCallback {
                override fun undo() {
                    dailyViewModel.addTodo(todo).observeOnce(viewLifecycleOwner) {
                        todoAdapter?.submitList(it)
                    }
                }

            },
            onDismissCallback = object : TodoCallback {
                override fun execute() {
                    Log.d(TAG, "execute: todo delete snackbar dismissed")
                }

            }
        ).observeOnce(viewLifecycleOwner){
            todoAdapter?.submitList(it)
        }

    }





    override fun onAddNoteClick(scheduleId: Long,programId:Long, dailyScheduleAdapter: TodoAdapter?) {
        showAddTodo(scheduleId,programId,dailyScheduleAdapter)
    }

    private fun showAddTodo(scheduleId: Long, programId: Long, todoAdapter: TodoAdapter?, todo:Todo?=null) {
        addTodoBtmSheetDialog= BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_add_todo,btn_sheet_add_todo)
        addTodoBtmSheetDialog.setContentView(view)
        addTodoBtmSheetDialog.behavior.state=BottomSheetBehavior.STATE_EXPANDED
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
                            todoAdapter?.submitList(it)
                        }
                }
                else{
                    dailyViewModel.updateTodo(todo.copy(title = task.toString())).observeOnce(viewLifecycleOwner){
                        todoAdapter?.submitList(it)
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
}