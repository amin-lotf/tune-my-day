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
class DeleteTodo @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(
        todo: Todo,
        undoCallback: SnackbarUndoCallback,
        onDismissCallback: TodoCallback
    ): Flow<DataState<Todo>?> {
        val cacheResponse = object : CacheResponseHandler<Int, Todo>() {
            override fun handleSuccess(resultObj: Int): DataState<Todo>? {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = DELETE_TODO_SUCCESS,
                            uiComponentType = UIComponentType.SnackBar(
                                undoCallback,
                                onDismissCallback
                            ),
                            messageType = MessageType.Success
                        ),
                        data = todo
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = DELETE_TODO_FAIL,
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
                    scheduleRepository.deleteTodo(todo)
                )
            }
        }
    }

    companion object {
        const val DELETE_TODO_SUCCESS = "Task deleted successfully"
        const val DELETE_TODO_FAIL = "Failed to delete the task"
    }

}