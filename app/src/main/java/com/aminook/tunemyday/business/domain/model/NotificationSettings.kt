package com.aminook.tunemyday.business.domain.model

data class NotificationSettings(
    var shouldRing:Boolean=true,
    var shouldVibrate:Boolean=true
)