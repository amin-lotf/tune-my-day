package com.aminook.tunemyday.util

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import java.lang.Exception


abstract class TimeTextWatcher( val maxVal: Int):TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        try {
            if (!s.isNullOrBlank() && s.length > 2) {
                val temp = s.toString().toInt() % 10

                setText(temp.toString())
                setSelection(1)
            } else if (!s.isNullOrBlank() && s.toString().toInt() > maxVal) {
                setText(maxVal.toString())
                setSelection(2)
            }
        } catch (e: Exception) {
        }
    }
    abstract fun setText(input:String)
    abstract fun setSelection(index:Int)

}