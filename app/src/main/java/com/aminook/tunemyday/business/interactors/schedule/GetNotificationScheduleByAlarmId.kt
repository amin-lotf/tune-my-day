package com.aminook.tunemyday.business.interactors.schedule

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNotificationScheduleByAlarmId @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(alarmId:Long): Flow<DataState<Schedule>?> {

        val cacheResponse=object :CacheResponseHandler<Schedule,Schedule>(){
            override  fun handleSuccess(resultObj: Schedule): DataState<Schedule>? {
                return if (resultObj.id!=0L){
                    DataState.data(
                        response = Response(
                            message = NOTIFICATION_SCHEDULE_RETRIEVED_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = resultObj)
                }
                else{
                    DataState.error(
                        response = Response(
                            message = NOTIFICATION_SCHEDULE_RETRIEVED_FAIL,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Error
                        )
                    )
                }

            }

        }

        return cacheResponse.getResult {
            flow {
               emit( scheduleRepository.getNotificationScheduleByAlarmId(alarmId))
            }
        }
    }

    companion object{
        const val NOTIFICATION_SCHEDULE_RETRIEVED_SUCCESS=" schedule retrieved successfully"
        const val NOTIFICATION_SCHEDULE_RETRIEVED_FAIL=" schedule does not exist"
    }
}