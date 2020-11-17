package com.aminook.tunemyday.business.interactors.settings

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsInteractors @Inject constructor(
    val getNotificationSettings: GetNotificationSettings,
    val updateNotificationSettings: UpdateNotificationSettings
)