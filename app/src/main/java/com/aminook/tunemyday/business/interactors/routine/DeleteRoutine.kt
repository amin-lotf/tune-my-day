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
class DeleteRoutine @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
      operator fun invoke(routineId: Long, activeRoutineId: Long):Flow<DataState<String>?>{
        val cacheResponse=object :CacheResponseHandler<Int,String>(){
            override fun handleSuccess(resultObj: Int): DataState<String>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = ROUTINE_DELETE_SUCCESS,
                            uiComponentType = UIComponentType.SnackBar(),
                            messageType = MessageType.Success
                        ),
                        data = ROUTINE_DELETE_SUCCESS
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = ROUTINE_DELETE_FAIL,
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
                    scheduleRepository.deleteRoutine(routineId,activeRoutineId)
                )
            }

        }
    }

    companion object{
        const val ROUTINE_DELETE_SUCCESS="Plan deleted"
        const val ROUTINE_DELETE_FAIL="Failed to delete routine"
    }
}