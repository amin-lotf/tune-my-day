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
class ScheduleUpcomingAlarmsByRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(routineId:Long):Flow<DataState<Boolean>?>{
        val cacheResponse=object :CacheResponseHandler<Boolean,Boolean>(){
            override fun handleSuccess(resultObj: Boolean): DataState<Boolean>? {
                return if (resultObj){
                    DataState.data(
                        response = Response(
                            message = CANCEL_ALARM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = resultObj
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = CANCEL_ALARM_FAIL,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }
            }
        }

        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.scheduleCurrentRoutineAlarms(routineId)
                )
            }
        }
    }

    companion object{
        const val CANCEL_ALARM_FAIL="Failed to cancel notifications"
        const val CANCEL_ALARM_SUCCESS="notifications canceled"
    }

}