package com.aminook.tunemyday.framework.datasource.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.model.ProgramEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ProgramDao {

    @Query("select * from programs")
    fun selectAllPrograms(): Flow<List<ProgramEntity>>

    @Query("select * from programs where id= :id")
    fun selectProgram(id:Int):Flow<ProgramEntity?>

    @Query("delete from programs")
    fun deleteAllPrograms(): Int

    @Delete
    fun deleteProgram(programEntity: ProgramEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProgram(programEntity: ProgramEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateProgram(programEntity: ProgramEntity): Int
}