package com.aminook.tunemyday.framework.presentation.addschedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.ToDo
import com.aminook.tunemyday.framework.presentation.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_add_schedule.*


class AddScheduleFragment : BaseFragment(R.layout.fragment_add_schedule) {
    private val TAG = "aminjoon"

    private var toDoListAdapter: ToDoListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toDoListAdapter = ToDoListAdapter()

        recycler_schedule_todo.apply {
            Log.d(TAG, "onViewCreated: recycler")
            layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter=toDoListAdapter
            setHasFixedSize(true)
        }

        toDoListAdapter?.submitList(listOf(ToDo("2","3",false,false)))
    }

    override fun onDestroy() {
        toDoListAdapter = null
        super.onDestroy()
    }
}