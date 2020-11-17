package com.aminook.tunemyday.util

import androidx.datastore.preferences.preferencesKey


const val SCHEDULE_REQUEST_NEW = "request new Schedule"
const val SCHEDULE_REQUEST_EDIT = "request edit Schedule"
val DAY_INDEX = preferencesKey<Int>("dayIndex")
val ROUTINE_INDEX = preferencesKey<Long>("routineIndex")
val SCREEN_TYPE = preferencesKey<String>("mainScreenType")
val VIBRATE_SETTINGS = preferencesKey<Boolean>("vibrateSettings")
val SOUND_SETTINGS = preferencesKey<Boolean>("soundSettings")

const val TYPE_VIBRATE="settings for vibrate"
const val TYPE_SOUND="settings for sound"

const val SCREEN_DAILY = "Show daily"
const val SCREEN_WEEKLY = "show weekly"
const val SCREEN_BLANK = "no active schedule"