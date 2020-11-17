package com.aminook.tunemyday.framework.presentation.nodata

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.SCREEN_BLANK
import com.aminook.tunemyday.util.SCREEN_DAILY
import com.aminook.tunemyday.util.SCREEN_WEEKLY
import com.aminook.tunemyday.util.navigateWithSourcePopUp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_no_data.*


@AndroidEntryPoint
class NoDataFragment : BaseFragment(R.layout.fragment_no_data) {

    private val viewModel:NoDataViewModel by viewModels()




    override fun onResume() {
        super.onResume()
        subscribeObservers()


    }



    private fun subscribeObservers() {
        viewModel.getScreenType().observe(viewLifecycleOwner){screenType->
            when(screenType){
                SCREEN_WEEKLY->{
                    findNavController().navigateWithSourcePopUp(R.id.noDataFragment,R.id.weeklyListFragment)
                }
                SCREEN_DAILY->{
                    findNavController().navigateWithSourcePopUp(R.id.noDataFragment,R.id.dailyFragment)
                }
                SCREEN_BLANK->{
                    initFragment()
                }
            }
        }
    }

    private fun initFragment() {
        canvas_container_routine.addView(NoRoutineCanvas(requireContext()))
    }

}