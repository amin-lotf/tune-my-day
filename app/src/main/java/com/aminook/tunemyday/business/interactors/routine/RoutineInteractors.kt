package com.aminook.tunemyday.business.interactors.routine

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineInteractors @Inject constructor(
    val insertRoutine: InsertRoutine,
    val getRoutine: GetRoutine,
    val getAllRoutine: GetAllRoutine,
    val updateRoutine: UpdateRoutine,
    val deleteRoutine: DeleteRoutine
)