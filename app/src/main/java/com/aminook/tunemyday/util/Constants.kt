package com.aminook.tunemyday.util

import androidx.datastore.preferences.preferencesKey


const val SCHEDULE_REQUEST_NEW="request new Schedule"
const val SCHEDULE_REQUEST_EDIT="request edit Schedule"
const val SCHEDULE_REQUEST_DELETE="request delete Schedule"
val DAY_INDEX= preferencesKey<Int>("dayIndex")