package com.aminook.tunemyday.business.interactors.settings

import com.aminook.tunemyday.business.data.cache.CacheResponseHandler
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.domain.model.NotificationSettings
import com.aminook.tunemyday.business.domain.state.DataState
import com.aminook.tunemyday.business.domain.state.MessageType
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNotificationSettings @Inject constructor(
    val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(): Flow<DataState<NotificationSettings>?>{
        val cacheResponse=object :CacheResponseHandler<NotificationSettings,NotificationSettings>(){
            override fun handleSuccess(resultObj: NotificationSettings): DataState<NotificationSettings>? {
                return DataState.data(
                    response = Response(
                        message = NOTIFICATION_SETTINGS_GET_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = resultObj
                )
            }
        }

        return cacheResponse.getResult {
            scheduleRepository.getNotificationSettings()
        }
    }

    companion object{
        const val NOTIFICATION_SETTINGS_GET_SUCCESS="notification settings received"
    }
}