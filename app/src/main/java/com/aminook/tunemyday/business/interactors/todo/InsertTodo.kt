package com.aminook.tunemyday.business.interactors.todo

import android.service.autofill.Dataset
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
class InsertTodo @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
     operator fun invoke(todo:Todo): Flow<DataState<Todo>?> {
        val cacheResponse=object :CacheResponseHandler<Long,Todo>(){
            override  fun handleSuccess(resultObj: Long): DataState<Todo>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = INSERT_TODO_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data = todo.copy(id = resultObj)
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = INSERT_TODO_FAIL,
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
                    scheduleRepository.insertTodo(todo)
                )
            }
        }

    }

    companion object{
        const val INSERT_TODO_SUCCESS="Task added to the checklist"
        const val INSERT_TODO_FAIL="Failed to add the task"
    }
}