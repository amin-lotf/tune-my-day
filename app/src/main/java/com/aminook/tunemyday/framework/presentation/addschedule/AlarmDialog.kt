package com.aminook.tunemyday.framework.presentation.addschedule

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Alarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_add_alarm.view.*
import java.lang.ClassCastException
import java.lang.Exception


//TODO( No Usage can be removed)
@AndroidEntryPoint
class AlarmDialog:DialogFragment() {
    private val TAG="aminjoon"
    private var isEditMode=false

    lateinit var alarmInEdit:Alarm
    val addScheduleViewModel:AddScheduleViewModel by viewModels()
    private lateinit var listener:OnAlarmSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder=AlertDialog.Builder(requireActivity())
        val view=layoutInflater.inflate(R.layout.layout_add_alarm,null)
        Log.d(TAG, "onCreateDialog: $addScheduleViewModel")
        val arg=arguments
        arg?.getParcelable<Alarm>("alarm")?.let {
            alarmInEdit=it.also {
                if(it.isAtStart){
                    view.chk_at_start.isChecked=true
                }else{
                    view.chk_at_start.isChecked=false
                    view.edt_alarm_hour.setText(it.hourBefore.toString())
                    view.edt_alarm_minute.setText(it.minuteBefore.toString())
                }
            }
        }

        view.edt_alarm_hour.setOnFocusChangeListener{ _, hasFocus->
            if(hasFocus){
                view.edt_alarm_hour.setText("")
            }

        }
        view.edt_alarm_minute.setOnFocusChangeListener{ _, hasFocus->
            if(hasFocus){
                view.edt_alarm_minute.setText("")
            }

        }

        view.edt_alarm_hour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                //TODO(Reformat it)
                val maxHour=23
                try {
                    if (!s.isNullOrBlank() && s.length > 2) {
                        val temp = s.toString().toInt() % 10

                        view.edt_alarm_hour.setText(temp.toString())
                        view.edt_alarm_hour.setSelection(1)
                    } else if (!s.isNullOrBlank() && s.toString().toInt() > maxHour) {
                        view.edt_alarm_hour.setText(maxHour.toString())
                        view.edt_alarm_hour.setSelection(2)
                    }
                } catch (e: Exception) {
                }
            }
        })

        view.edt_alarm_minute.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                //TODO(Reformat it)
                val maxMinute=59
                try {
                    if (!s.isNullOrBlank() && s.length > 2) {
                        val temp = s.toString().toInt() % 10

                        view.edt_alarm_minute.setText(temp.toString())
                        view.edt_alarm_minute.setSelection(1)
                    } else if (!s.isNullOrBlank() && s.toString().toInt() > maxMinute) {
                        view.edt_alarm_minute.setText(maxMinute.toString())
                        view.edt_alarm_minute.setSelection(2)
                    }
                } catch (e: Exception) {
                }
            }
        })

        builder.setView(view)
            .setTitle("Add Reminder")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Done",object :DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    var hour=0
                    var minute=0
                    if(!view.chk_at_start.isChecked){

                        hour=view.edt_alarm_hour.text.toString().toIntOrNull()?:0
                        minute=view.edt_alarm_minute.text.toString().toIntOrNull()?:0
                    }
                    if(!this@AlarmDialog::alarmInEdit.isInitialized){
                        alarmInEdit= Alarm(hourBefore = hour,minuteBefore = minute)
                    }else{
                        alarmInEdit.let {
                            it.hourBefore=hour
                            it.minuteBefore=minute
                        }
                    }

                    addScheduleViewModel.setAlarm(alarmInEdit)
                }

            })
        return builder.create()

    }

//    override fun onAttach(context: Context) {
//        try {
//            listener=targetFragment as OnAlarmSetListener
//        }catch (e:ClassCastException){
//            Log.d(TAG, "onAttach Alarm Dialog: Fragment didn't implement listener")
//        }catch (e:Exception){
//            Log.d(TAG, "onAttach: ${e.message}")
//        }
//        super.onAttach(context)
//    }



    interface OnAlarmSetListener{
        fun onAlarmSet(hour:Int,minute:Int,isEditMode:Boolean,alarmInEdit:Int?)
    }

}