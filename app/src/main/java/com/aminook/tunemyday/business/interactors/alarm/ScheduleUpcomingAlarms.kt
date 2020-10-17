package com.aminook.tunemyday.business.interactors.alarm

import androidx.datastore.DataStore
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
class ScheduleUpcomingAlarms @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(alarms: List<Alarm>):Flow<DataState<Boolean>?>{
        val cacheResponse=object :CacheResponseHandler<Boolean,Boolean>(){
            override fun handleSuccess(resultObj: Boolean): DataState<Boolean>? {
                return if (resultObj){
                    DataState.data(
                        response = Response(
                            message = SCHEDULE_ALARM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = resultObj
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = SCHEDULE_ALARM_FAIL,
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
                    scheduleRepository.scheduleUpComingAlarms(alarms)
                )
            }
        }
    }

    companion object{
        const val SCHEDULE_ALARM_FAIL="Failed to set notification"
        const val SCHEDULE_ALARM_SUCCESS="notification set"
    }

}