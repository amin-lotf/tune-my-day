package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.framework.datasource.model.ProgramEntity
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    fun updateProgram(programEntity: ProgramEntity):Int

    fun insertProgram(programEntity: ProgramEntity):Long

    fun selectAllPrograms(): Flow<List<ProgramEntity>>

    fun selectProgram(id:Int):Flow<ProgramEntity?>

    fun deleteAllPrograms(): Int

    fun deleteProgram(programEntity: ProgramEntity): Int
}