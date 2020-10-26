package com.aminook.tunemyday.framework.datasource.cache.database

import androidx.room.*
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("select * from routines  where id=:routineId")
    fun getRoutine(routineId:Long): Flow<RoutineEntity>

    @Query("select * from routines")
    fun getAllRoutines():Flow<List<RoutineEntity>>

    @Insert
    suspend fun insertRoutine(routineEntity: RoutineEntity):Long

    @Update
    suspend fun updateRoutine(routineEntity: RoutineEntity):Int

    @Query("delete from routines where id=:routineId")
    suspend fun deleteRoutine(routineId: Long):Int

}