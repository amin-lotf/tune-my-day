package com.aminook.tunemyday.framework.presentation.addschedule.manager

import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule

class AddScheduleManager {

    private val buffSchedule=Schedule()

    fun addProgramToBuffer(program: Program){
        buffSchedule.program=program
    }

    fun removeProgramFromBuffer(){
        buffSchedule.program=null
    }

    val isValid:Boolean
    get() = buffSchedule.program!=null

}