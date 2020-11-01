package com.aminook.tunemyday.util

import androidx.datastore.preferences.preferencesKey


const val SCHEDULE_REQUEST_NEW="request new Schedule"
const val SCHEDULE_REQUEST_EDIT="request edit Schedule"
val DAY_INDEX= preferencesKey<Int>("dayIndex")
val ROUTINE_INDEX= preferencesKey<Long>("routineIndex")
val SCREEN_TYPE= preferencesKey<String>("mainScreenType")
val SCREEN_DAILY="Show daily"
val SCREEN_WEEKLY="show weekly"
val SCREEN_BLANK="no active schedule"