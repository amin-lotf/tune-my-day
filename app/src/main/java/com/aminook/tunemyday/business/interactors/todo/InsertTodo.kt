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
    suspend operator fun invoke(todo:Todo): Flow<DataState<String>?> {
        val cacheResponse=object :CacheResponseHandler<Long,String>(){
            override suspend fun handleSuccess(resultObj: Long): DataState<String>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = INSERT_TODO_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = INSERT_TODO_SUCCESS
                    )
                }else{
                    DataState.data(
                        response = Response(
                            message = INSERT_TODO_FAIL,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        data = INSERT_TODO_FAIL
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
        const val INSERT_TODO_SUCCESS="todo inserted successfully"
        const val INSERT_TODO_FAIL="failed to insert todo"
    }
}