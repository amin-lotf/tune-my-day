package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.database.ScheduleDao
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list),
    WeekViewPagerAdapter.WeeklyRecyclerViewListener {

    private val TAG = "aminjoon"

    @Inject
    lateinit var days:List<Day>


     var weekViewPagerAdapter: WeekViewPagerAdapter?=null

    @Inject
    lateinit var scheduleDao: ScheduleDao

    //lateinit var shortDailyScheduleRecycler: ShortDailyScheduleRecycler


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_schedule.setOnClickListener {
            val action=WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment()
            findNavController().navigate(action)
        }

        weekViewPagerAdapter= WeekViewPagerAdapter(days).apply {
            setListener(this@WeeklyListFragment)
        }
       // shortDailyScheduleRecycler = ShortDailyScheduleRecycler()
       

        weekly_view_pager.apply {
            this.adapter = weekViewPagerAdapter

        }

        TabLayoutMediator(weekly_tab_layout, weekly_view_pager) { tab, position ->
            val day = days[position]
            tab.text = day.shortName

        }.attach()


    }

    override fun setAdapter(itemView: WeekViewPagerAdapter.ViewHolder, position: Int) {
        val shortDailyScheduleRecycler = ShortDailyScheduleRecycler()
        itemView.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = shortDailyScheduleRecycler

            val schedules1 = mutableListOf<Schedule>()
            schedules1.add(Schedule(1, 1, "Gym", "3h", "19:45", "20:00", true, false))
            schedules1.add(
                Schedule(
                    1,
                    1,
                    "Programming",
                    "3h",
                    "20:45",
                    "21:00",
                    false,
                    true
                )
            )

            val schedules2 = mutableListOf<Schedule>()
            schedules2.add(Schedule(1, 1, "Study", "3h", "19:45", "20:00", true, false))
            schedules2.add(Schedule(1, 1, "Rest", "3h", "20:45", "21:00", false, true))

            val allSchedules= listOf(schedules1,schedules2,schedules1,schedules2,schedules1,schedules2,schedules1)
            shortDailyScheduleRecycler.submitList(allSchedules[position])


        }


    }


    override fun onPause() {

        weekly_view_pager.adapter=null
        weekViewPagerAdapter=null
        super.onPause()

    }

}