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
class UpdateRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke(routineEntity: RoutineEntity):Flow<DataState<Nothing>?>{
        val cacheResponse=object :CacheResponseHandler<Int,Nothing>(){
            override fun handleSuccess(resultObj: Int): DataState<Nothing>? {

                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = ROUTINE_UPDATE_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = null)
                }else{
                    DataState.error(
                        response = Response(
                            message = ROUTINE_UPDATE_FAIL,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        )
                    )
                }


            }
        }

        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.updateRoutine(routineEntity)
                )
            }

        }
    }

    companion object{
        const val ROUTINE_UPDATE_SUCCESS="routine updated"
        const val ROUTINE_UPDATE_FAIL="Failed to update routine"
    }
}