package com.aminook.tunemyday.business.interactors.todo

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateTodos @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke(todos:List<Todo>,scheduleId:Long): Flow<DataState<List<Todo>>?> {
        val cacheResponse=object :CacheResponseHandler<Int,List<Todo>>(){
            override  fun handleSuccess(resultObj: Int): DataState<List<Todo>>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = UPDATE_TODO_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = todos
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = UPDATE_TODO_FAIL,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }
            }

        }

        return  cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.updateTodos(todos)
                )
            }
        }

    }

    companion object{
        const val UPDATE_TODO_SUCCESS="todo updated"
        const val UPDATE_TODO_FAIL="failed to update tasks"
    }
}