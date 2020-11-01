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
class GetAlarmById @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(alarmId: Long): Flow<DataState<Alarm>?> {
        val cacheResponse = object : CacheResponseHandler<Alarm, Alarm>() {
            override fun handleSuccess(resultObj: Alarm): DataState<Alarm>? {
                return DataState.data(
                    response = Response(
                        message = ALARM_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }
        }

        return cacheResponse.getResult {

            flow {
                emit(scheduleRepository.getAlarmById(alarmId))
            }

        }
    }

    companion object {
        val ALARM_GET_SUCCESS = "alarm retrieved successfully"
    }
}