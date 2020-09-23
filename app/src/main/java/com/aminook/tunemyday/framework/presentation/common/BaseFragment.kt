package com.aminook.tunemyday.framework.presentation.common

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.aminook.tunemyday.framework.presentation.AlarmController
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.UIController

abstract class BaseFragment constructor(
    @LayoutRes private val layoutRes: Int

):Fragment(layoutRes){

     var uiController: UIController?=null
     var alarmController: AlarmController?=null

    override fun onAttach(context: Context) {
        setupControllers()
        super.onAttach(context)
    }


    fun setupControllers(){
        try {
            (requireActivity() as MainActivity).let {
                uiController=it
                alarmController=it
            }
        }catch (e:ClassCastException){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        uiController=null
        alarmController=null
        super.onDestroy()
    }


}