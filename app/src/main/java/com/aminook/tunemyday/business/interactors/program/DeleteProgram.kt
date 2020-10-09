package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.*


import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteProgram @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    suspend operator  fun invoke(
        program:ProgramDetail,
        snackbarUndoCallback: SnackbarUndoCallback?=null,
        onDismissCallback: TodoCallback?=null): Flow<DataState<Nothing>?> {

        val cacheResponse=object :CacheResponseHandler<Int,Nothing>(){
            override fun handleSuccess(resultObj: Int): DataState<Nothing>? {
                return if(resultObj>0){
                    DataState.data(
                        response = Response(
                            message = DELETE_PROGRAM_SUCCEED,
                            uiComponentType = UIComponentType.SnackBar(snackbarUndoCallback,onDismissCallback),
                            messageType = MessageType.Success
                        ),
                        data = null
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = DELETE_PROGRAM_FAIL,
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
                    scheduleRepository.deleteProgram(program)
                )
            }
        }


    }

    companion object{
        const val DELETE_PROGRAM_SUCCEED=" program deleted successfully"
        const val DELETE_PROGRAM_FAIL="Failed to delete program"
    }
}