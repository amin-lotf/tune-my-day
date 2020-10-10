package com.aminook.tunemyday.framework.presentation.weeklylist

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.datastore.DataStore
import androidx.datastore.createDataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.impl.model.Preference
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.business.interactors.schedule.InsertSchedule
import com.aminook.tunemyday.framework.datasource.cache.database.ScheduleDao
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.DAY_INDEX
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list),
    WeekViewPagerAdapter.WeeklyRecyclerViewListener, ItemClickListener {

    private val TAG = "aminjoon"


    private val viewModel: WeeklyListViewModel by viewModels()

    @Inject
    lateinit var days: List<Day>




    var weekViewPagerAdapter: WeekViewPagerAdapter? = null
    var shortDailyScheduleRecycler: ShortDailyScheduleRecycler? = null

    @Inject
    lateinit var scheduleDao: ScheduleDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWeeklyViewPager()
        setupTabLayout()
        subscribeObservers()


    }

    private fun setupWeeklyViewPager() {
        weekViewPagerAdapter = WeekViewPagerAdapter().apply {
            setListener(this@WeeklyListFragment)
            submitList(days)
        }

        weekly_view_pager.apply {
            this.adapter = weekViewPagerAdapter
            this.visibility = View.VISIBLE
        }
    }

    private fun setupTabLayout() {
        TabLayoutMediator(weekly_tab_layout, weekly_view_pager) { tab, position ->
            val day = days[position]
            tab.text = day.shortName


        }.attach()

        weekly_tab_layout.apply {

            setTabTextColors(
                ContextCompat.getColor(requireContext(), R.color.colorDark),
                ContextCompat.getColor(requireContext(), R.color.label4)
            )
            setSelectedTabIndicatorColor(
                ContextCompat.getColor(requireContext(), R.color.label4)
            )

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.savedDayIndex=tab?.position?:0
                    viewModel.setSavedDayIndex()

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
        }

    }


    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response, null)
            }
        }

        viewModel.getAllSchedules()
    }

    override fun setAdapter(itemView: WeekViewPagerAdapter.ViewHolder, position: Int) {


        shortDailyScheduleRecycler = ShortDailyScheduleRecycler(requireContext())
        shortDailyScheduleRecycler?.setOnClickListener(this)


        itemView.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = shortDailyScheduleRecycler
            viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
                shortDailyScheduleRecycler?.submitList(schedules.filter { it.startDay == position })

            }

        }
        if (viewModel.isFirstLoad){
            viewModel.isFirstLoad=false
            viewModel.getSavedDayIndex().observeOnce(viewLifecycleOwner){dayIndex->
                weekly_tab_layout.getTabAt(dayIndex)?.select()
                Log.d(TAG, "setAdapter: first")
            }

        }

    }


    override fun onPause() {

        weekViewPagerAdapter = null
        shortDailyScheduleRecycler = null
        super.onPause()

    }

    override fun onItemClick(schedule: Schedule) {
        val action = WeeklyListFragmentDirections.actionWeeklyListFragmentToAddScheduleFragment(
            scheduleRequestType = SCHEDULE_REQUEST_EDIT,
            scheduleId = schedule.id
        )
        findNavController().navigate(action)
    }

}