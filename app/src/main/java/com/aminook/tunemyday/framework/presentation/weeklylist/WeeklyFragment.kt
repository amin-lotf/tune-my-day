package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import dagger.hilt.android.AndroidEntryPoint
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
            it.getLong(ROUTINE_INDEX_PARAM).let { index->
                viewModel.fragmentRoutineIndex=index
            }
        }
//        CoroutineScope(IO).launch {
//            delay(200)
//           withContext(Main){
               setupAdapter()
//           }
//        }

    }




    private fun setupAdapter() {
        shortDailyScheduleAdapter= ShortDailyScheduleAdapter(requireContext())
        shortDailyScheduleAdapter?.setOnClickListener(this)

        viewModel.getFragmentSchedules().observe(viewLifecycleOwner){
            Log.d(TAG, "setupAdapter: size: ${it.size} ")
            shortDailyScheduleAdapter?.submitList(it)
        }

        daily_short_schedules_recycler.apply {
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=shortDailyScheduleAdapter
        }

    }


    companion object {

        const val DAY_INDEX_PARAM = "param1"
        const val ROUTINE_INDEX_PARAM = "param2"

        @JvmStatic
        fun newInstance(dayIndex: Int, routineIndex: Long) =
            WeeklyFragment().apply {
                arguments = Bundle().apply {
                    putInt(DAY_INDEX_PARAM, dayIndex)
                    putLong(ROUTINE_INDEX_PARAM, routineIndex)
                }
            }
    }

    override fun onItemClick(schedule: Schedule) {
        val action = WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
            scheduleRequestType = SCHEDULE_REQUEST_EDIT,
            scheduleId = schedule.id
        )
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        shortDailyScheduleAdapter?.setOnClickListener(null)
        shortDailyScheduleAdapter=null
        super.onDestroy()
    }

}