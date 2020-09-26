package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.ToDo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.daily_schedule_item.*
import kotlinx.android.synthetic.main.fragment_daily.*

@AndroidEntryPoint
class DailyFragment : Fragment(R.layout.fragment_daily), ToDoAdapter.ToDoRecyclerViewListener,
    DailyScheduleAdapter.DailyScheduleAdapterListener {
    private val TAG="aminjoon"
    private var fragmentIndex:Int?=null
    private val dailyViewModel:DailyViewModel by viewModels()

    private var toDoAdapter:ToDoAdapter?=null
    private var subToDoAdapter:SubToDoAdapter?=null
    private var dailyScheduleAdapter:DailyScheduleAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            fragmentIndex = it.getInt(DAY_INDEX)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeAdapters()
    }

    private fun initializeAdapters() {
        dailyScheduleAdapter=DailyScheduleAdapter()
        dailyScheduleAdapter?.setListener(this)
        recycler_day_schedule.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=dailyScheduleAdapter
        }

    }



    override fun setSubTodoAdapter(itemView: ToDoAdapter.ViewHolder, todo: ToDo) {
       subToDoAdapter= SubToDoAdapter()
        itemView.subTodoRecycler.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=subToDoAdapter
        }
    }


    override fun onDestroy() {
        toDoAdapter=null
        subToDoAdapter=null
        dailyScheduleAdapter=null
        super.onDestroy()

    }



    override fun setTodoAdapter(holder: DailyScheduleAdapter.ViewHolder, schedule: Schedule) {
        toDoAdapter=ToDoAdapter()
        toDoAdapter?.setListener(this)
        recycler_schedule_todo.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=toDoAdapter
        }
    }

    override fun onAddNoteClick(scheduleId: Long) {

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