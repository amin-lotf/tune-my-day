package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.android.synthetic.main.fragment_daily_list.*


class DailyListFragment : BaseFragment(R.layout.fragment_daily_list) {
    private val TAG="aminjoon"
    var dailyAdapter:DayViewPagerAdapter?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dailyAdapter=DayViewPagerAdapter(requireActivity())
        view_pager_daily.adapter=dailyAdapter
    }

    override fun onDestroy() {
        dailyAdapter=null
        super.onDestroy()
    }



}