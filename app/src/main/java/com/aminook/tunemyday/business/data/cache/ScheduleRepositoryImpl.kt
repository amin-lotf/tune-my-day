package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.framework.datasource.cache.database.*
import com.aminook.tunemyday.framework.datasource.cache.mappers.Mappers
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
   val daoService: DaoService,
   val mappers: Mappers
):ScheduleRepository {
    override suspend fun updateProgram(program: Program):Int {
        return daoService.programDao.updateProgram(
            mappers.programCacheMapper.mapToEntity(program)
        )
    }

    override suspend fun insertProgram(program: Program): Long {
        return daoService.programDao.insertProgram(
            mappers.programCacheMapper.mapToEntity(program)
        )
    }

    override fun selectAllPrograms(): Flow<List<Program>> {
        return  daoService.programDao.selectAllPrograms().map { entityList->
            entityList.map {entity->
                mappers.programCacheMapper.mapFromEntity(entity)
            }
        }
    }

    override fun selectProgram(id: Int): Flow<Program?> {
        return daoService.programDao.selectProgram(id).map {entity->
            entity?.let {
                mappers.programCacheMapper.mapFromEntity(it)
            }
        }
    }

    override suspend fun deleteAllPrograms(): Int{
       return daoService.programDao.deleteAllPrograms()
    }

    override suspend fun deleteProgram(program: Program): Int {
       return  daoService.programDao.deleteProgram(
           mappers.programCacheMapper.mapToEntity(program)
       )
    }


}