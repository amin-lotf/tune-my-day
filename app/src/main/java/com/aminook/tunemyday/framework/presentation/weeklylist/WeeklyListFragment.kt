package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DayFactory
import com.aminook.tunemyday.framework.datasource.cache.database.ScheduleDao
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_DELETE
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list),
    WeekViewPagerAdapter.WeeklyRecyclerViewListener, ItemClickListener {

    private val TAG = "aminjoon"

    private val viewModel: WeeklyListViewModel by viewModels()

    @Inject
    lateinit var days: List<Day>

    @Inject
    lateinit var dateUtil:DayFactory

    var weekViewPagerAdapter: WeekViewPagerAdapter? = null
    var shortDailyScheduleRecycler:ShortDailyScheduleRecycler?=null
    @Inject
    lateinit var scheduleDao: ScheduleDao

    //lateinit var shortDailyScheduleRecycler: ShortDailyScheduleRecycler


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: weekly list")
        weekViewPagerAdapter = WeekViewPagerAdapter().apply {
            setListener(this@WeeklyListFragment)
        }
         shortDailyScheduleRecycler = ShortDailyScheduleRecycler()




        weekViewPagerAdapter?.submitList(days)
        weekly_view_pager.apply {
            this.adapter = weekViewPagerAdapter
            this.currentItem=dateUtil.curDayIndex
            this.visibility=View.VISIBLE
        }


        TabLayoutMediator(weekly_tab_layout, weekly_view_pager) { tab, position ->
            val day = days[position]
            tab.text = day.shortName


        }.attach()

        //weekly_tab_layout.getTabAt(3)?.select()


        subscribeObservers()
    }


    private fun subscribeObservers() {


//        viewModel.daysOfWeek.observe(viewLifecycleOwner) { days ->
//
//            var selectedDay = viewModel.selectedDay?:days.find { day -> day.isChosen }?.dayIndex ?: 1
//
//            weekViewPagerAdapter?.submitList(days)
//            weekly_view_pager.apply {
//                this.adapter = weekViewPagerAdapter
//                this.currentItem=3
//
//            }
//            TabLayoutMediator(weekly_tab_layout, weekly_view_pager) { tab, position ->
//                val day = days[position]
//                tab.text = day.shortName
//
//            }.attach()
//            //weekly_view_pager.currentItem = selectedDay
//
//            //weekly_tab_layout.getTabAt(selectedDay)?.select()
//            //TODO(Fix the scrolling Issue)
////            Handler(Looper.getMainLooper()).postDelayed(
////                {
////                    Log.d(TAG, "subscribeObservers: $selectedDay")
////                    weekly_tab_layout.getTabAt(selectedDay)?.select()
////                },
////                100
////            )
//        }
//       viewModel.catchDaysOfWeek()
        viewModel.getAllSchedules()


    }

    override fun setAdapter(itemView: WeekViewPagerAdapter.ViewHolder, position: Int) {
        shortDailyScheduleRecycler = ShortDailyScheduleRecycler()
        shortDailyScheduleRecycler?.setOnClickListener(this)
        itemView.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = shortDailyScheduleRecycler
            viewModel.selectedDay=position
            viewModel.schedules.removeObservers(viewLifecycleOwner)
            viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
                shortDailyScheduleRecycler?.submitList(schedules.filter { it.startDay == position })
            }
        }


    }


    override fun onPause() {

        //weekly_view_pager.adapter = null
        weekViewPagerAdapter = null
        shortDailyScheduleRecycler=null
        super.onPause()

    }

    override fun onItemClick(schedule: Schedule) {
        val action=WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
            scheduleRequestType = SCHEDULE_REQUEST_EDIT,
            scheduleId = schedule.id
            )
        findNavController().navigate(action)
    }

}