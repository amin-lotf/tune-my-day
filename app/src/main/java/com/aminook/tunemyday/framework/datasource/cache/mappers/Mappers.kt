package com.aminook.tunemyday.framework.datasource.cache.mappers

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Mappers @Inject constructor(
    val programCacheMapper: ProgramCacheMapper,
    val fullScheduleCacheMapper: FullScheduleCacheMapper,
    val alarmCacheMapper: AlarmCacheMapper,
    val subTodoCacheMapper: SubTodoCacheMapper,
    val todoCacheMapper: TodoCacheMapper
)