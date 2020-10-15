package com.aminook.tunemyday.business.interactors.schedule

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.state.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValidateSchedule @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {
      operator fun invoke(schedule:Schedule,areYouSureCallback: AreYouSureCallback):Flow<DataState<List<Schedule>>?>{
        val cacheResponse=object :CacheResponseHandler<List<Schedule>,List<Schedule>>(){
            override  fun handleSuccess(resultObj: List<Schedule>): DataState<List<Schedule>>? {

                return if(resultObj.isEmpty()){
                    DataState.data(
                        response = Response(
                            message = SCHEDULE_NO_OVERWRITE,
                            uiComponentType = UIComponentType.None,
                            messageType = MessageType.Info
                        ),
                        data = resultObj
                    )
                }else{
                    val tmp=if (resultObj.size==1) "activity" else "activities"
                    DataState.data(
                        response = Response(
                            message = "This activity will overwrite ${resultObj.size} $tmp",
                            uiComponentType = UIComponentType.AreYouSureDialog(areYouSureCallback),
                            messageType = MessageType.Info
                        ),
                        data = resultObj
                    )
                }
            }

        }

        return cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.checkIfOverwrite(schedule)
                )
            }

        }
    }

    companion object{
        val SCHEDULE_NO_OVERWRITE=" schedule does not overwrite other schedules"
    }
}