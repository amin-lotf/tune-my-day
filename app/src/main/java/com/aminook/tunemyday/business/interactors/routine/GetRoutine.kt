package com.aminook.tunemyday.business.interactors.routine

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GetRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke(routineId:Long):Flow<DataState<RoutineEntity>?>{
        val cacheResponse=object :CacheResponseHandler<RoutineEntity,RoutineEntity>(){
            override fun handleSuccess(resultObj: RoutineEntity): DataState<RoutineEntity>? {
                return DataState.data(
                    response = Response(
                        message = ROUTINE_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj

                )
            }
        }

        return cacheResponse.getResult {
            scheduleRepository.getRoutine(routineId)
        }
    }

    companion object{
        const val ROUTINE_GET_SUCCESS="routine found"
        const val ROUTINE_GET_FAIL="Failed to load routine"
    }
}