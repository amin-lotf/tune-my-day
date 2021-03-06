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
class InsertSchedule @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(schedule:Schedule,conflictedSchedule:List<Schedule>,requestType:String,curRoutine: Long):Flow<DataState<String>?>{
        val cacheResponse=object : CacheResponseHandler<Long,String>() {
            override  fun handleSuccess(resultObj: Long): DataState<String>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = INSERT_SCHEDULE_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = INSERT_SCHEDULE_SUCCESS
                    )
                }else{
                    DataState.data(
                        response = Response(
                            message = INSERT_Schedule_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = INSERT_Schedule_FAILED
                    )
                }
            }

        }
        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.insertModifySchedule(schedule,conflictedSchedule,requestType,curRoutine)
                )
            }

        }
    }

    companion object {
       const val INSERT_SCHEDULE_SUCCESS = "Activity added to your schedule"
        const val INSERT_Schedule_FAILED = "Failed to add activity"
    }
}