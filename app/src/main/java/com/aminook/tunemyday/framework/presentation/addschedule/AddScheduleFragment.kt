package com.aminook.tunemyday.framework.presentation.addschedule

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.presentation.common.BaseFragment


class AddScheduleFragment : BaseFragment(R.layout.fragment_add_schedule) {
    private val TAG="aminjoon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
    }
}