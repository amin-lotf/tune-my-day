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
import com.aminook.tunemyday.util.observeOnce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.*
import kotlinx.android.synthetic.main.bottom_sheet_add_routine.view.*
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list){

    private val TAG = "aminjoon"

    private lateinit var addRoutineBtmSheetDialog: BottomSheetDialog
    private val viewModel: WeeklyListViewModel by viewModels()
    var weeklyViewPagerAdapter: WeeklyViewPagerAdapter?=null
    @Inject
    lateinit var days: List<Day>





    override fun onResume() {
        weekly_view_pager.adapter=null
        subscribeObservers()
        setupToolbar()
        super.onResume()

    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { stateMessage ->
                onResponseReceived(stateMessage.response)
            }
        }

        viewModel.getRoutineIndex().observe(viewLifecycleOwner) {
            viewModel.getRoutine(it).observe(viewLifecycleOwner){routine->
                if (routine != null && routine.id != 0L) {
                    toolbar_weekly_schedule.title=routine.name
                    setupWeeklyViewPager(routine)
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
                    val action =
                        WeeklyListFragmentDirections.actionWeeklyListFragmentToRoutineFragment(
                            viewModel.routineId
                        )
                    findNavController().navigate(action)
                    true
                }

                else -> false

            }
        }
    }

    private fun showAddRoutineDialog() {
        addRoutineBtmSheetDialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_DialogStyle)
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
        viewModel.getDayIndex().observeOnce(viewLifecycleOwner){
            weekly_view_pager.postDelayed( {
                weekly_view_pager.currentItem=it
            },10)
            layout_weekly_parent.postDelayed({
                layout_weekly_parent.visibility = View.VISIBLE
            }, 200)
        }
    }

    override fun onPause() {
        weekly_view_pager.adapter=null
        weeklyViewPagerAdapter=null
        viewModel.saveDayIndex(weekly_tab_layout.selectedTabPosition)
        super.onPause()
    }




}