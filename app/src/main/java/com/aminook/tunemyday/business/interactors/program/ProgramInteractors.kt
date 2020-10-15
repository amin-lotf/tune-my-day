package com.aminook.tunemyday.business.interactors.program

import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProgramInteractors @Inject constructor(
    val insertProgram:InsertProgram,
    val getAllPrograms: GetAllPrograms,
    val getAllDetailedPrograms: GetAllDetailedPrograms,
    val deleteProgram: DeleteProgram,
    val undoDeletedProgram: UndoDeletedProgram,
    val updateProgram: UpdateProgram,
    val getDetailedProgram: GetDetailedProgram,
    val getProgram: GetProgram
)