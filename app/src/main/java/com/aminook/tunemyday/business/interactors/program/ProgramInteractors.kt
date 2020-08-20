package com.aminook.tunemyday.business.interactors.program

import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ProgramInteractors @Inject constructor(
    val insertProgram:InsertProgram
)