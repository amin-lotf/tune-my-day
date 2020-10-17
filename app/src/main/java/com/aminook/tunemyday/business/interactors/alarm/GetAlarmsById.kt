package com.aminook.tunemyday.business.interactors.alarm

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAlarmsById @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(alarmIds: List<Long>): Flow<DataState<List<Alarm>>?> {
        val cacheResponse = object : CacheResponseHandler<List<Alarm>, List<Alarm>>() {
            override fun handleSuccess(resultObj: List<Alarm>): DataState<List<Alarm>>? {
                return DataState.data(
                    response = Response(
                        message = ALARMS_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {

            flow {
                emit(scheduleRepository.getAlarmsById(alarmIds))
            }

        }
    }

    companion object {
        val ALARMS_GET_SUCCESS = "alarms retrieved successfully"
    }
}