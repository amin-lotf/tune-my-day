package com.aminook.tunemyday.business.interactors.alarm

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUpcomingAlarmIdsByRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(routineId:Long): Flow<DataState<List<Long>>?> {
        val cacheResponse = object : CacheResponseHandler<List<Long>, List<Long>>() {
            override  fun handleSuccess(resultObj: List<Long>): DataState<List<Long>>? {
                return DataState.data(
                    response = Response(
                        message = ALARM__IDS_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {
            flow {
               emit( scheduleRepository.getUpcomingAlarmIdsByRoutine(routineId))
            }

        }
    }

    companion object {
        val ALARM__IDS_GET_SUCCESS = "alarm Ids retrieved successfully"
    }
}