package com.aminook.tunemyday.business.interactors.program

import android.util.Log
import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertProgram @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {

      operator fun invoke(program: Program): Flow<DataState<Program>?> {
        val cacheResponse = object : CacheResponseHandler<Long, Program>() {
            override  fun handleSuccess(resultObj: Long): DataState<Program>? {
                return if (resultObj > 0) {

                    DataState.data(
                        response = Response(
                            message = INSERT_PROGRAM_SUCCESS,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Success
                        ),
                        data = program.copy(id=resultObj)
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = INSERT_PROGRAM_FAILED,
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
        const val INSERT_PROGRAM_SUCCESS = "Successfully inserted new program"
        const val INSERT_PROGRAM_FAILED = "Failed to insert new program"
    }
}