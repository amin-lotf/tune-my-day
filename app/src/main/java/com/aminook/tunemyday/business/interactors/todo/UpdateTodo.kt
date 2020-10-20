package com.aminook.tunemyday.business.interactors.todo

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateTodo @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(
        todo: Todo,
        showSnackbar: Boolean = true,
        undoCallback: SnackbarUndoCallback? = null,
        onDismissCallback: TodoCallback? = null
    ): Flow<DataState<Todo>?> {
        val cacheResponse = object : CacheResponseHandler<Int, Todo>() {
            override fun handleSuccess(resultObj: Int): DataState<Todo>? {
                return if (resultObj > 0) {

                    DataState.data(
                        response = Response(
                            message = UPDATE_TODO_SUCCESS,
                            uiComponentType =
                            if (showSnackbar)
                                UIComponentType.SnackBar(undoCallback, onDismissCallback)
                            else
                                UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = todo
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = UPDATE_TODO_FAIL,
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
                    scheduleRepository.updateTodo(todo)
                )
            }
        }

    }

    companion object {
        const val UPDATE_TODO_SUCCESS = "Task updated"
        const val UPDATE_TODO_FAIL = "Failed to update the task"
    }
}