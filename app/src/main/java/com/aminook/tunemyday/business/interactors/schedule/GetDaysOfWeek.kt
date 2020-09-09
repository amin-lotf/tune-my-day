package com.aminook.tunemyday.business.interactors.schedule

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDaysOfWeek @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    suspend operator fun invoke(chosenDay:Int=1): Flow<DataState<List<Day>>?>{
        val cacheResponse=object :CacheResponseHandler<List<Day>,List<Day>>() {
            override suspend fun handleSuccess(resultObj: List<Day>): DataState<List<Day>>? {
                return DataState.data(
                    response = Response(
                        message = GET_DAYS_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }

        }

        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.getDaysOfWeek(chosenDay)
                )
            }

        }
    }
    companion object {
        val GET_DAYS_SUCCESS = "Successfully received days of week."
        val INSERT_DAYS_FAILED = "Failed to received days of week."
    }
}