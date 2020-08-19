package com.aminook.tunemyday.business.data.util

import com.aminook.tunemyday.business.data.cache.CacheResult
import com.aminook.tunemyday.business.data.util.CacheConstants.CACHE_TIMEOUT
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_TIMEOUT
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_UNKNOWN
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

suspend fun <T> safeCacheCall(
    dispatcher:CoroutineDispatcher,
    cacheCall: () -> Flow<T>
): CacheResult<T?> {
    return withContext(dispatcher){
        try {
            withTimeout(CACHE_TIMEOUT){
               CacheResult.Success(cacheCall().single())
            }
        }catch (throwable:Throwable){
            when (throwable) {

                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}