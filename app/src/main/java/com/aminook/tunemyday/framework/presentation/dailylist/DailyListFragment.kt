package com.aminook.tunemyday.framework.presentation.dailylist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_daily_list.*


class DailyListFragment : BaseFragment(R.layout.fragment_daily_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_test.setOnClickListener {
            findNavController().navigate(R.id.action_dailyListFragment_to_addScheduleFragment)
        }
    }
}