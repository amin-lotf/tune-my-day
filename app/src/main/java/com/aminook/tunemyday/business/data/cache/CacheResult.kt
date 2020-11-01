package com.aminook.tunemyday.business.data.cache

sealed class CacheResult<out T> {
    data class Success<out T>(val value:T):CacheResult<T>()
}