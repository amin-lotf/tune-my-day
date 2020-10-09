package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail

import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged


@Dao
interface ProgramDao {

    @Query("select * from programs")
    fun selectAllPrograms(): Flow<List<ProgramEntity>>

    @Query("select * from programs where id= :id")
    fun selectProgram(id:Int):Flow<ProgramEntity?>

    @Query("delete from programs")
    fun deleteAllPrograms(): Int

    @Delete
    suspend fun deleteProgram(programEntity: ProgramEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgram(programEntity: ProgramEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateProgram(programEntity: ProgramEntity): Int

    fun getAllProgramsDistinctUntilChanged()=selectAllPrograms().distinctUntilChanged()


    @Transaction
    @Query("select * from programs order by name")
    fun getProgramsSummary():Flow<List<ProgramDetail>>




}