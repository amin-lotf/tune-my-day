package com.aminook.tunemyday.framework.presentation.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.aminook.tunemyday.framework.presentation.addschedule.AddScheduleFragment
import com.aminook.tunemyday.framework.presentation.addtodo.AddToDoFragment
import com.aminook.tunemyday.framework.presentation.dailylist.DailyListFragment
import com.aminook.tunemyday.framework.presentation.tasklist.TaskListFragment
import com.aminook.tunemyday.framework.presentation.weeklylist.WeeklyListFragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppFragmentFactory @Inject constructor() : FragmentFactory() {


    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            WeeklyListFragment::class.java.name -> WeeklyListFragment()

            DailyListFragment::class.java.name->DailyListFragment()

            TaskListFragment::class.java.name->TaskListFragment()

            AddScheduleFragment::class.java.name->AddScheduleFragment()

            AddToDoFragment::class.java.name->AddToDoFragment()

            else->WeeklyListFragment()
        }
    }
}