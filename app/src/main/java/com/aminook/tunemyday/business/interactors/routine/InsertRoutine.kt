package com.aminook.tunemyday.business.interactors.routine

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(routineEntity: RoutineEntity): Flow<DataState<Long>?>{
        val cacheResponse=object :CacheResponseHandler<Long,Long>(){
            override fun handleSuccess(resultObj: Long): DataState<Long>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = ROUTINE_INSERT_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = resultObj
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = ROUTINE_INSERT_FAIL,
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
                    scheduleRepository.insertRoutine(routineEntity)
                )
            }
        }
    }

    companion object{
        const val ROUTINE_INSERT_SUCCESS="Weekly schedule created"
        const val ROUTINE_INSERT_FAIL="Failed to create weekly schedule"
    }

}