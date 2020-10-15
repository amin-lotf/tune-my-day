package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDetailedProgram @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(programId:Long): Flow<DataState<ProgramDetail>?> {
        val cacheResponse = object : CacheResponseHandler<ProgramDetail,ProgramDetail>() {
            override  fun handleSuccess(resultObj: ProgramDetail): DataState<ProgramDetail>? {
                return DataState.data(
                    response = Response(
                        message = GET_PROGRAM_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.None
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {
            scheduleRepository.getProgramDetail(programId)
        }
    }

    companion object {
        val GET_PROGRAM_SUCCESS = "Successfully retrieved all programs from the cache."
        val GET_PROGRAM_FAILED = "Failed to get programs from the cache."
    }

}