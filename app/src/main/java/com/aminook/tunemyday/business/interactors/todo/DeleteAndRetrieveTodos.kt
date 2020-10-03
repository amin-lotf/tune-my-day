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
class DeleteAndRetrieveTodos @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(todo:Todo, undoCallback: SnackbarUndoCallback, onDismissCallback: TodoCallback): Flow<DataState<List<Todo>>?>{

        val cacheResponse=object :CacheResponseHandler<List<Todo>,List<Todo>>(){
            override fun handleSuccess(resultObj: List<Todo>): DataState<List<Todo>>? {
                return DataState.data(
                    response = Response(
                        message = DELETE_AND_RETRIEVE_TODO_SUCCESS,
                        uiComponentType = UIComponentType.SnackBar(
                            undoCallback,
                            onDismissCallback
                        ),
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }
        }

        return  cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.deleteAndRetrieveTodos(todo)
                )
            }
        }
    }

    companion object{
        const val DELETE_AND_RETRIEVE_TODO_SUCCESS="task deleted successfully"
        const val DELETE_AND_RETRIEVE_TODO_FAIL="Failed to delete task"
    }
}