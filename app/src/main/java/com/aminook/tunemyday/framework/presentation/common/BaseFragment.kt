package com.aminook.tunemyday.framework.presentation.common

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.UIController

abstract class BaseFragment constructor(
    @LayoutRes private val layoutRes: Int

):Fragment(layoutRes){

    lateinit var uiController: UIController


    override fun onAttach(context: Context) {
        setupUIController()
        super.onAttach(context)
    }


    fun setupUIController(){
        try {
            (requireActivity() as MainActivity).let {
                uiController=it
            }
        }catch (e:ClassCastException){
            e.printStackTrace()
        }
    }


}