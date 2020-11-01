package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetProgram @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

     operator fun invoke(programId:Long): Flow<DataState<Program>?> {
        val cacheResponse = object : CacheResponseHandler<Program,Program>() {
            override  fun handleSuccess(resultObj: Program): DataState<Program>? {
                return if (resultObj.id!=0L){
                    DataState.data(
                        response = Response(
                            message = GET_PROGRAM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.None
                        ),
                        data = resultObj
                    )
                }else{
                    DataState.error(
                        response = Response(
                            message = GET_PROGRAM_FAILED,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }
            }

        }

        return cacheResponse.getResult {
            scheduleRepository.getProgram(programId)
        }
    }

    companion object {
       const val GET_PROGRAM_SUCCESS = "Successfully retrieved  program from cache."
       const val GET_PROGRAM_FAILED = "Failed to retrieve programs"
    }

}