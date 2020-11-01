package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllPrograms @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(): Flow<DataState<List<Program>>?> {
        val cacheResponse = object : CacheResponseHandler<List<Program>,List<Program>>() {
            override  fun handleSuccess(resultObj: List<Program>): DataState<List<Program>>? {
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

            scheduleRepository.getAllPrograms()
        }
    }

    companion object {
        const val GET_PROGRAMS_SUCCESS = "Successfully retrieved all programs from the cache."
    }

}