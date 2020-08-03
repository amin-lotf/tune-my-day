package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.framework.datasource.database.AlarmDao
import com.aminook.tunemyday.framework.datasource.database.ProgramDao
import com.aminook.tunemyday.framework.datasource.database.ToDoDao
import com.aminook.tunemyday.framework.datasource.database.ToDoScheduleDao
import com.aminook.tunemyday.framework.datasource.model.ProgramEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    val scheduleDao: ToDoScheduleDao,
    val programDao: ProgramDao,
    val alarmDao: AlarmDao,
    val toDoDao: ToDoDao,
    val toDoScheduleDao: ToDoScheduleDao
):ScheduleRepository {
    override fun updateProgram(programEntity: ProgramEntity):Int {
        return programDao.updateProgram(programEntity)
    }

    override fun insertProgram(programEntity: ProgramEntity): Long {
        return programDao.insertProgram(programEntity)
    }

    override fun selectAllPrograms(): Flow<List<ProgramEntity>> {
        return  programDao.selectAllPrograms()
    }

    override fun selectProgram(id: Int): Flow<ProgramEntity?> {
        return programDao.selectProgram(id)
    }

    override fun deleteAllPrograms(): Int{
       return programDao.deleteAllPrograms()
    }

    override fun deleteProgram(programEntity: ProgramEntity): Int {
       return  programDao.deleteProgram(programEntity)
    }


}