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
class GetAllSchedules @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(routineId:Long): Flow<DataState<List<Schedule>>?> {

        val cacheResult=object :CacheResponseHandler<List<Schedule>,List<Schedule>>(){
            override  fun handleSuccess(resultObj: List<Schedule>): DataState<List<Schedule>>? {
                return DataState.data(
                    response = Response(
                        message = ALL_SCHEDULES_RECEIVED,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }

        }

        return  cacheResult.getResult {
            scheduleRepository.getAllSchedules(routineId)
        }
    }

    companion object{
        const val ALL_SCHEDULES_RECEIVED="Successfully received all schedules"
    }
}