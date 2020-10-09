package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllDetailedPrograms @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(): Flow<DataState<List<ProgramDetail>>?> {
        val cacheResponse = object : CacheResponseHandler<List<ProgramDetail>,List<ProgramDetail>>() {
            override  fun handleSuccess(resultObj: List<ProgramDetail>): DataState<List<ProgramDetail>>? {
                return DataState.data(
                    response = Response(
                        message = GET_PROGRAMS_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.None
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {

            scheduleRepository.getAllProgramsDetail()
        }
    }

    companion object {
        val GET_PROGRAMS_SUCCESS = "Successfully retrieved all programs from the cache."
        val GET_PROGRAMS_FAILED = "Failed to get programs from the cache."
    }

}