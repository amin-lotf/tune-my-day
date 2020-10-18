package com.aminook.tunemyday.framework.presentation.routine.manager

import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity

class RoutineManager {

    fun processRoutines(routines:List<RoutineEntity>):List<RoutineEntity>{
        val tmpRoutines= mutableListOf<RoutineEntity>()
        tmpRoutines.addAll(routines)
        tmpRoutines.add(RoutineEntity("empty"))
        return tmpRoutines
    }
}