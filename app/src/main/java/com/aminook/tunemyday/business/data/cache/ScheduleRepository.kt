package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    suspend fun updateProgram(program: Program):Int

    suspend fun insertProgram(program: Program):Long

    fun selectAllPrograms(): Flow<List<Program>>

    fun selectProgram(id:Int):Flow<Program?>

    suspend fun deleteAllPrograms(): Int

    suspend fun deleteProgram(program: Program): Int
}