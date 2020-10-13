package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.database.ScheduleDao
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import kotlinx.android.synthetic.main.weekly_item_view_pager.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list),
    WeekViewPagerAdapter.WeeklyRecyclerViewListener, ItemClickListener {

    private val TAG = "aminjoon"

    private lateinit var addRoutineBtmSheetDialog: BottomSheetDialog
    private val viewModel: WeeklyListViewModel by viewModels()

    @Inject
    lateinit var days: List<Day>


    var weekViewPagerAdapter: WeekViewPagerAdapter? = null
    var shortDailyScheduleRecycler: ShortDailyScheduleRecycler? = null

    @Inject
    lateinit var scheduleDao: ScheduleDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getRoutineIndex().observeOnce(viewLifecycleOwner){
            Log.d(TAG, "onViewCreated: routineid: $it")
            viewModel.getRoutine(it)
        }

        subscribeObservers()
        setupToolbar()
    }


    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                uiController?.onResponseReceived(stateMessage.response, null)
            }
        }

        viewModel.routine.observe(viewLifecycleOwner) {
            Log.d(TAG, "subscribeObservers: routine id: $it")
            if (it!=null && it.id!=0L ){

                layout_weekly_parent.visibility=View.VISIBLE
                txt_weekly_no_routine.visibility=View.GONE
                setupWeeklyViewPager()
                setupTabLayout()
                viewModel.getAllSchedules(it.id)


            }else{
                layout_weekly_parent.visibility=View.GONE
                txt_weekly_no_routine.visibility=View.VISIBLE
            }
        }




    }


    private fun setupToolbar() {
        toolbar_weekly_schedule.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_new_weekly -> {
                    showAddRoutineDialog()
                    true
                }

                R.id.action_load_weekly -> {
                    val action=R.id.action_weeklyListFragment_to_routineFragment
                    findNavController().navigate(action)
                    true
                }

                else -> false

            }
        }
    }

    private fun showAddRoutineDialog() {

        addRoutineBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        addRoutineBtmSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_routine, btn_sheet_add_routine)
        addRoutineBtmSheetDialog.setContentView(view)
        addRoutineBtmSheetDialog.show()

        view.txt_add_routine.requestFocus()

        view.btn_save_routine.setOnClickListener {
            if (view.txt_add_routine.text.isNotBlank()) {

                viewModel.addRoutine(view.txt_add_routine.text.toString())
                addRoutineBtmSheetDialog.dismiss()
            }
        }
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
                    Log.d(TAG, "onTabSelected: ${tab?.position}")
                    viewModel.savedDayIndex = tab?.position ?: 0
                    viewModel.SaveDayIndex()

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
        }

    }



    override fun setAdapter(itemView: WeekViewPagerAdapter.ViewHolder, position: Int) {

        Log.d(TAG, "setAdapter: $position ")
        shortDailyScheduleRecycler = ShortDailyScheduleRecycler(requireContext())
        shortDailyScheduleRecycler?.setOnClickListener(this)


        itemView.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = shortDailyScheduleRecycler
            viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
                shortDailyScheduleRecycler?.submitList(schedules.filter { it.startDay == position })
            }
        }
        if (viewModel.isFirstLoad) {
            viewModel.isFirstLoad = false
            viewModel.getDayIndex().observeOnce(viewLifecycleOwner) { dayIndex ->
                weekly_tab_layout.getTabAt(dayIndex)?.select()
                Log.d(TAG, "setAdapter: first $dayIndex")
            }

        }
        else{
            Log.d(TAG, "setAdapter: not first")
        }

    }


    override fun onPause() {
        Log.d(TAG, "onPause: weekly")
        viewModel.isFirstLoad=true
        weekViewPagerAdapter = null
        shortDailyScheduleRecycler = null
        weekly_view_pager.adapter=null
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