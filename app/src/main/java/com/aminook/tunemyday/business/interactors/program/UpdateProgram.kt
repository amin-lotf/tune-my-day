package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
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
class UpdateProgram @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    suspend operator  fun invoke(program:Program):Flow<DataState<Nothing>?>{

        val cacheResponse=object :CacheResponseHandler<Int,Nothing>(){
            override fun handleSuccess(resultObj: Int): DataState<Nothing>? {
                return  if (resultObj>0){
                    DataState.data(
                        response = Response(
                            message = PROGRAM_UPDATE_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = null
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = PROGRAM_UPDATE_FAIL,
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
                    scheduleRepository.updateProgram(program)
                )
            }
        }

    }

    companion object{
        const val PROGRAM_UPDATE_SUCCESS="activity updated successfully"
        const val PROGRAM_UPDATE_FAIL="Failed to update the activity"
    }
}