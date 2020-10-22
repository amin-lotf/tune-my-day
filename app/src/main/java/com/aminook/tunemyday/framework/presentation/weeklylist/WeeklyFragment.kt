package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_weekly.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class WeeklyFragment : BaseFragment(R.layout.fragment_weekly), ItemClickListener {

    private val TAG="aminjoon"
    private val viewModel: WeeklyViewModel by viewModels()
    private var shortDailyScheduleAdapter:ShortDailyScheduleAdapter?= null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            it.getInt(DAY_INDEX_PARAM).let { index ->
                viewModel.fragmentDayIndex = index
            }
            it.getLong(ROUTINE_INDEX_PARAM).let { index ->
                Log.d(TAG, "onViewCreated: weekly routineId: $index")
                viewModel.fragmentRoutineIndex = index
            }
        }
        setupAdapter()

        (requireActivity() as MainActivity).fab_schedule.setOnClickListener {
            val action =
                WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
                    scheduleRequestType = SCHEDULE_REQUEST_NEW
                )
            findNavController().navigate(action)
        }
    }


    
    private fun setupAdapter() {
        shortDailyScheduleAdapter= ShortDailyScheduleAdapter(requireContext(),viewModel.fragmentDayIndex)
        shortDailyScheduleAdapter?.setOnClickListener(this)

        viewModel.getFragmentSchedules().observe(viewLifecycleOwner){
            Log.d(TAG, "setupAdapter: size: ${it.size} ")
            shortDailyScheduleAdapter?.submitList(it)
        }

        daily_short_schedules_recycler.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=shortDailyScheduleAdapter
        }


        daily_short_schedules_recycler.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(dy>0){
                    (requireActivity().findViewById<FloatingActionButton>(R.id.fab_schedule)).hide()
                }else{
                    (requireActivity().findViewById<FloatingActionButton>(R.id.fab_schedule)).show()
                }
            }
        })

    }


    companion object {

        const val DAY_INDEX_PARAM = "param1"
        const val ROUTINE_INDEX_PARAM = "param2"

        @JvmStatic
        fun newInstance(dayIndex: Int, routineIndex: Long) =
            WeeklyFragment().apply {
                Log.d(TAG, "newInstance: routineId $routineIndex")
                arguments = Bundle().apply {
                    putInt(DAY_INDEX_PARAM, dayIndex)
                    putLong(ROUTINE_INDEX_PARAM, routineIndex)
                }
            }
    }

    override fun onItemClick(schedule: Schedule) {
        val action = if(schedule.id!= -1L) {
             WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
                scheduleRequestType = SCHEDULE_REQUEST_EDIT,
                scheduleId = schedule.id
            )
        }else{
            WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
                scheduleRequestType = SCHEDULE_REQUEST_NEW,
                scheduleId = schedule.id,
                startTime = schedule.startTime,
                endTime = schedule.endTime
            )
        }
        findNavController().navigate(action)
    }


    override fun onPause() {
        shortDailyScheduleAdapter=null
        super.onPause()
    }





}