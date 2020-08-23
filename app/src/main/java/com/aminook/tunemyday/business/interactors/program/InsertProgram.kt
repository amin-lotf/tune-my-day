package com.aminook.tunemyday.business.interactors.program

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertProgram @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {

    private val TAG="aminjoon"
    suspend  operator fun invoke(program: Program): Flow<DataState<Program>?> {
        val cacheResponse = object : CacheResponseHandler<Long, Program>() {
            override suspend fun handleSuccess(resultObj: Long): DataState<Program>? {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = InsertProgram.INSERT_PROGRAM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = program
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = InsertProgram.INSERT_PROGRAM_FAILED,
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
                    scheduleRepository.insertProgram(program)
                )
            }
        }

    }

    companion object {
        val INSERT_PROGRAM_SUCCESS = "Successfully inserted new program."
        val INSERT_PROGRAM_FAILED = "Failed to insert new program."
    }
}