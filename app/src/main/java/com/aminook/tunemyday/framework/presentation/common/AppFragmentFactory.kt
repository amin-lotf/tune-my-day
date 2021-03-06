package com.aminook.tunemyday.framework.presentation.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.aminook.tunemyday.framework.presentation.addroutine.AddRoutineFragment
import com.aminook.tunemyday.framework.presentation.viewtodos.ViewTodoFragment
import com.aminook.tunemyday.framework.presentation.addschedule.AddEditScheduleFragment
import com.aminook.tunemyday.framework.presentation.ProgramList.ProgramListFragment
import com.aminook.tunemyday.framework.presentation.addprogram.AddProgramFragment
import com.aminook.tunemyday.framework.presentation.dailylist.DailyFragment
import com.aminook.tunemyday.framework.presentation.nodata.NoDataFragment
import com.aminook.tunemyday.framework.presentation.routine.RoutineFragment
import com.aminook.tunemyday.framework.presentation.weeklylist.WeeklyListFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppFragmentFactory @Inject constructor() : FragmentFactory() {


    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            WeeklyListFragment::class.java.name -> WeeklyListFragment()

            ProgramListFragment::class.java.name->ProgramListFragment()

            AddEditScheduleFragment::class.java.name->AddEditScheduleFragment()

            DailyFragment::class.java.name->DailyFragment()

            RoutineFragment::class.java.name->RoutineFragment()

            AddProgramFragment::class.java.name->AddProgramFragment()

            ViewTodoFragment::class.java.name-> ViewTodoFragment()

            NoDataFragment::class.java.name-> NoDataFragment()

            AddRoutineFragment::class.java.name-> AddRoutineFragment()

            else->WeeklyListFragment()
        }
    }
}