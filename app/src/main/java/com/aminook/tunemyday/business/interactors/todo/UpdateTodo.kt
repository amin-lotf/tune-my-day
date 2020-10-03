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
class UpdateTodo @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke(todo:Todo): Flow<DataState<List<Todo>>?> {
        val cacheResponse=object :CacheResponseHandler<List<Todo>,List<Todo>>(){
            override  fun handleSuccess(resultObj: List<Todo>): DataState<List<Todo>>? {
                return DataState.data(
                        response = Response(
                            message = UPDATE_TODO_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = resultObj
                    )
            }

        }

        return  cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.updateAndRetrieveTodos(todo)
                )
            }
        }

    }

    companion object{
        const val UPDATE_TODO_SUCCESS="todo updated"
        const val UPDATE_TODO_FAIL="failed to update todo"
    }
}