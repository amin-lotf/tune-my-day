package com.aminook.tunemyday.framework.presentation.weeklylist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.DayFactory
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_weekly_list.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class WeeklyListFragment : BaseFragment(R.layout.fragment_weekly_list) {

    @Inject
    lateinit var dayFactory: DayFactory

    val texts= listOf("1","2","3","4","5","6","7")




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val days=dayFactory.getNextSevenDays(Date())
        val adapter=WeekViewPagerAdapter(texts)


        weekly_view_pager.adapter=adapter

        TabLayoutMediator(weekly_tab_layout,weekly_view_pager){tab,position->
            val day=days[position]
            tab.text= "${day.shortName}"

        }.attach()
    }

}