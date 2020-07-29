package com.aminook.tunemyday.framework.presentation.common

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment constructor(
    @LayoutRes private val layoutRes: Int

):Fragment(layoutRes){

}