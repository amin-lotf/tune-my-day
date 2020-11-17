package com.aminook.tunemyday.business.interactors.settings

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.NotificationSettings
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNotificationSettings @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(notificationSettings: NotificationSettings): Flow<DataState<Boolean>?>{
        val cacheResponse=object :CacheResponseHandler<Unit,Boolean>(){
            override fun handleSuccess(resultObj: Unit): DataState<Boolean>? {
                return DataState.data(
                    response = Response(
                        message = NOTIFICATION_SETTINGS_UPDATE_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = true
                )
            }
        }

        return  cacheResponse.getResult {
            flow {
                emit(
                    scheduleRepository.updateNotificationSettings(notificationSettings)
                )
            }
        }
    }
    companion object{
        const val NOTIFICATION_SETTINGS_UPDATE_SUCCESS="notification settings updated"
    }
}