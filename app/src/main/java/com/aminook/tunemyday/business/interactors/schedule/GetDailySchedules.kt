package com.aminook.tunemyday.business.interactors.schedule

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDailySchedules @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(dayIndex:Int,routineId:Long):Flow<DataState<List<Schedule>>?>{

        val cacheResponse=object :CacheResponseHandler<List<Schedule>,List<Schedule>>(){
            override  fun handleSuccess(resultObj: List<Schedule>): DataState<List<Schedule>>? {
                return DataState.data(
                    response = Response(
                        message = DAILY_SCHEDULES_RECEIVED,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {
            scheduleRepository.getDailySchedules(dayIndex,routineId)
        }

    }

    companion object{
        val DAILY_SCHEDULES_RECEIVED="Successfully received daily schedules"
    }
}