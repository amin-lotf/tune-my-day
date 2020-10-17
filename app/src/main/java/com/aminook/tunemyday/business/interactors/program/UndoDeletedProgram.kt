package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UndoDeletedProgram @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    suspend operator fun invoke(program:ProgramDetail,curRoutine: Long):Flow<DataState<Nothing>?>{

        val cacheResponse=object :CacheResponseHandler<Long,Nothing>(){
            override fun handleSuccess(resultObj: Long): DataState<Nothing>? {
                return if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = UNDO_PROGRAM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = null
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = UNDO_PROGRAM_FAILED,
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
                    scheduleRepository.undoDeletedProgram(program,curRoutine)
                )
            }
        }
    }

    companion object{
        val UNDO_PROGRAM_SUCCESS = "Successfully returned the program"
        val UNDO_PROGRAM_FAILED = "Failed to undo the action"
    }
}