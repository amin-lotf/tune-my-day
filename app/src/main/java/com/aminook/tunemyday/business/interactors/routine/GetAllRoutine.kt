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
class GetAllRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke():Flow<DataState<List<RoutineEntity>>?>{
        val cacheResponse=object :CacheResponseHandler<List<RoutineEntity>,List<RoutineEntity>>(){
            override fun handleSuccess(resultObj: List<RoutineEntity>): DataState<List<RoutineEntity>>? {
                return DataState.data(
                    response = Response(
                        message = ROUTINES_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj

                )
            }
        }

        return cacheResponse.getResult {
            scheduleRepository.getAllRoutines()
        }
    }

    companion object{
        const val ROUTINES_GET_SUCCESS="routines found"
        const val ROUTINES_GET_FAIL="Failed to get routines"
    }
}