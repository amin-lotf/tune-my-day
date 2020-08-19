package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.data.util.safeCacheCall
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.framework.presentation.addschedule.state.AddScheduleViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow

class InsertProgram(
    private val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(program: Program, stateEvent: StateEvent) =
        flow {

            val cacheResult = safeCacheCall(IO) {
                flow {
                    emit(scheduleRepository.insertProgram(program))
                }
            }

            val cacheResponse = object : CacheResponseHandler<AddScheduleViewState, Long>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Long): DataState<AddScheduleViewState>? {
                    return if (resultObj > 0) {
                        val viewState = AddScheduleViewState(
                            newProgram = program
                        )
                        DataState.data(
                            response = Response(
                                message = INSERT_PROGRAM_SUCCESS,
                                uiComponentType = UIComponentType.None,
                                messageType = MessageType.Success
                            ),
                            stateEvent = stateEvent
                        )
                    } else {
                        DataState.data(
                            response = Response(
                                message = INSERT_PROGRAM_FAILED,
                                uiComponentType = UIComponentType.Toast,
                                messageType = MessageType.Error
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }

            }.getResult()
            emit(cacheResponse)

        }

    companion object {
        val INSERT_PROGRAM_SUCCESS = "Successfully inserted new program."
        val INSERT_PROGRAM_FAILED = "Failed to insert new program."
    }
}