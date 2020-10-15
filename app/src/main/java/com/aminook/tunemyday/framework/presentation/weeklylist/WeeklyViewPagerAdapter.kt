package com.aminook.tunemyday.framework.presentation.weeklylist

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aminook.tunemyday.framework.presentation.weeklylist.WeeklyFragment

class WeeklyViewPagerAdapter(fm:FragmentManager,lc:Lifecycle,val routineIndex:Long):FragmentStateAdapter(fm,lc) {
    private val TAG="aminjoon"
    private val dayCount=7
    override fun getItemCount()=dayCount

    override fun createFragment(position: Int): Fragment {
        Log.d(TAG, "createFragment: pager= $position")
       return WeeklyFragment.newInstance(position,routineIndex)
    }
}