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
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.DAY_INDEX
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_EDIT
import com.aminook.tunemyday.util.SCHEDULE_REQUEST_NEW
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list){

    private val TAG = "aminjoon"

    private lateinit var addRoutineBtmSheetDialog: BottomSheetDialog
    private val viewModel: WeeklyListViewModel by viewModels()

    @Inject
    lateinit var days: List<Day>


    var weeklyViewPagerAdapter: WeeklyViewPagerAdapter?=null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        weekly_view_pager.adapter=null
        subscribeObservers()
        setupToolbar()
    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }
        }



        viewModel.getRoutineIndex().observeOnce(viewLifecycleOwner) {

            viewModel.getRoutine(it).observe(viewLifecycleOwner){
                if (it != null && it.id != 0L) {
                    Log.d(TAG, "subscribeObservers: routine : $it")
                    toolbar_weekly_schedule.title=it.name
                    txt_weekly_no_routine.visibility = View.GONE
                    layout_weekly_parent.visibility = View.INVISIBLE
                    setupWeeklyViewPager(it)

                } else {
                    toolbar_weekly_schedule.title="Weekly schedule"
                    layout_weekly_parent.visibility = View.GONE
                    txt_weekly_no_routine.visibility = View.VISIBLE
                }
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
                    val action = R.id.action_weeklyListFragment_to_routineFragment
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

        val view =
            layoutInflater.inflate(R.layout.bottom_sheet_add_routine, btn_sheet_add_routine)
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

    private fun setupWeeklyViewPager(routine:RoutineEntity) {
               weeklyViewPagerAdapter= WeeklyViewPagerAdapter(childFragmentManager,viewLifecycleOwner.lifecycle,routine.id)
               weekly_view_pager.apply {
                   this.adapter = weeklyViewPagerAdapter
                   this.visibility = View.VISIBLE
               }
               setupTabLayout()
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
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
        }
        viewModel.getDayIndex().observeOnce(viewLifecycleOwner){
            Log.d(TAG, "setupTabLayout: dayIndex: $it")
            weekly_view_pager.postDelayed( {
                weekly_view_pager.currentItem=it
            },10)
            layout_weekly_parent.postDelayed({
                layout_weekly_parent.visibility = View.VISIBLE
            }, 200)

        }


    }


    override fun onPause() {
        viewModel.saveDayIndex(weekly_tab_layout.selectedTabPosition)
        Log.d(TAG, "onPause weekly list: weekly")
        layout_weekly_parent.visibility = View.INVISIBLE
        txt_weekly_no_routine.visibility = View.INVISIBLE
        weekly_view_pager.adapter=null
        weeklyViewPagerAdapter=null
        super.onPause()
    }



}