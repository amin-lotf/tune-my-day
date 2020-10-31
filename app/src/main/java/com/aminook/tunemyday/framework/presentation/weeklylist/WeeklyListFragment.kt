package com.aminook.tunemyday.framework.presentation.weeklylist

import android.graphics.Color.TRANSPARENT
import android.graphics.PixelFormat.TRANSPARENT
import android.icu.lang.UCharacter.JoiningType.TRANSPARENT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCREEN_BLANK
import com.aminook.tunemyday.util.SCREEN_DAILY
import com.aminook.tunemyday.util.SCREEN_WEEKLY
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list) {

    private val TAG = "aminjoon"


    private val viewModel: WeeklyListViewModel by viewModels()
    var weeklyViewPagerAdapter: WeeklyViewPagerAdapter? = null

    @Inject
    lateinit var days: List<Day>



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }


    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }
        }

        viewModel.getScreenType().observeOnce(viewLifecycleOwner) { screenType ->
            when (screenType) {
                SCREEN_WEEKLY -> {
                    setupWeeklyListFragment()
                }
                SCREEN_DAILY -> {
                    val action =
                        WeeklyListFragmentDirections.actionWeeklyListFragmentToDailyFragment()
                    findNavController().navigate(action)
                }
                SCREEN_BLANK -> {
                    val action =
                        WeeklyListFragmentDirections.actionWeeklyListFragmentToNoDataFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupWeeklyListFragment() {
        app_bar_weekly_list.visibility = View.VISIBLE
        viewModel.getRoutineIndex().observe(viewLifecycleOwner) {
            viewModel.getRoutine(it).observe(viewLifecycleOwner) { routine ->
                if (routine != null && routine.id != 0L) {
                    toolbar_weekly_schedule.title = routine.name
                    setupWeeklyViewPager(routine)
                } else {
                    val action =
                        WeeklyListFragmentDirections.actionWeeklyListFragmentToNoDataFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupWeeklyViewPager(routine: RoutineEntity) {
            weeklyViewPagerAdapter =
                WeeklyViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, routine.id)
        if(weekly_view_pager.adapter==null){
            weekly_view_pager.apply {
                this.adapter = weeklyViewPagerAdapter
                this.offscreenPageLimit=7
            }
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
        }

        weekly_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                requireActivity().bottom_app_bar.performShow()
                viewModel.saveDayIndex(weekly_tab_layout.selectedTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        viewModel.getDayIndex().observeOnce(viewLifecycleOwner) {
            weekly_view_pager.postDelayed({
                weekly_view_pager.currentItem = it
            }, 10)
            layout_weekly_parent.postDelayed({
                layout_weekly_parent.visibility = View.VISIBLE
            }, 400)
        }
    }

    override fun onPause() {
        weeklyViewPagerAdapter=null
        super.onPause()
    }

}