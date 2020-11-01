package com.aminook.tunemyday.business.interactors.schedule

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.util.TodoCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteSchedule @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(
        schedule: Schedule,
        snackbarUndoCallback: SnackbarUndoCallback? = null,
        onDismissCallback: TodoCallback? = null
    ): Flow<DataState<Nothing>?> {
        val cacheResponse = object : CacheResponseHandler<Int, Nothing>() {
            override fun handleSuccess(resultObj: Int): DataState<Nothing>? {
                if (resultObj > 0) {
                    return DataState.data(
                        response = Response(
                            message = SCHEDULE_DELETE_SUCCESS,
                            uiComponentType = UIComponentType.SnackBar(
                                snackbarUndoCallback,
                                onDismissCallback
                            ),
                            messageType = MessageType.Success
                        )

                    )
                } else {
                    return DataState.error(
                        response = Response(
                            message = SCHEDULE_DELETE_FAIL,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        )
                    )
                }
            }

        }

        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.deleteSchedule(schedule)
                )
            }
        }
    }

    companion object {
        const val SCHEDULE_DELETE_SUCCESS = "schedule deleted successfully"
        const val SCHEDULE_DELETE_FAIL = "Failed to delete schedule"
    }
}