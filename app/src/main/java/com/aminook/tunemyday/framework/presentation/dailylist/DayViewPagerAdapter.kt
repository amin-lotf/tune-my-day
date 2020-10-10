package com.aminook.tunemyday.framework.presentation.dailylist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class DayViewPagerAdapter(fm:FragmentManager,lc:Lifecycle):FragmentStateAdapter(fm,lc){
    private val dayCount=7


    override fun createFragment(position: Int): Fragment {
        return DailyFragment.newInstance(position)
    }

    override fun getItemCount()=dayCount
}