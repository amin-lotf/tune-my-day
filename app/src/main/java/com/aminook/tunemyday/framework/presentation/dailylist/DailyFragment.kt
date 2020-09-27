package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.*
import kotlinx.android.synthetic.main.bottom_sheet_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_daily.*

@AndroidEntryPoint
class DailyFragment : BaseFragment(R.layout.fragment_daily), ToDoAdapter.ToDoRecyclerViewListener,
    DailyScheduleAdapter.DailyScheduleAdapterListener {
    private val TAG="aminjoon"
    private var fragmentIndex:Int?=null
    private val dailyViewModel:DailyViewModel by viewModels()
    private lateinit var addTodoBtmSheetDialog:BottomSheetDialog

    private var toDoAdapter:ToDoAdapter?=null
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

        initializeAdapters()
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



    override fun setTodoAdapter(holder: DailyScheduleAdapter.ViewHolder, schedule: Schedule) {
        toDoAdapter=ToDoAdapter()
        toDoAdapter?.setListener(this)
        holder.todoRecyclerView.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=toDoAdapter
        }
    }

    override fun onDestroy() {
        toDoAdapter=null
        subToDoAdapter=null
        dailyScheduleAdapter=null
        super.onDestroy()

    }

    override fun onAddNoteClick(scheduleId: Long) {
        showAddTodo(scheduleId)
    }

    private fun showAddTodo(scheduleId: Long) {
        addTodoBtmSheetDialog= BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_add_todo,btn_sheet_add_todo)
        addTodoBtmSheetDialog.setContentView(view)
        addTodoBtmSheetDialog.show()
        view.txt_add_todo.requestFocus()
        view.btn_save_todo.setOnClickListener {
            if(!view.txt_add_todo.text.isNullOrBlank()){
                val task=view.txt_add_todo.text
                dailyViewModel.addTodo(scheduleId,task.toString())
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
}