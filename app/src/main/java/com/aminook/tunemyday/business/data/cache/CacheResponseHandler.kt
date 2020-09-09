package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.data.util.CacheConstants
import com.aminook.tunemyday.business.data.util.ErrorConstants
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_DATA_NULL
import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_ERROR_UNKNOWN
import com.aminook.tunemyday.business.domain.state.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


abstract class CacheResponseHandler<CacheDataType,OutputType>(
) {

    suspend fun getResult(cacheCall: () -> Flow<CacheDataType?>): Flow<DataState<OutputType>?> {
        return try {
            cacheCall().map {data->
                if (data==null){
                    DataState.error(
                        Response(
                            message = CACHE_DATA_NULL,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )
                    )
                }else{
                    handleSuccess(data)
                }
            }
        } catch (throwable: Throwable) {
             flow {
                emit(
                    DataState.error<OutputType>(
                        response = Response(
                            message = CACHE_ERROR_UNKNOWN,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        )

                    )
                )
            }
        }
    }
    companion object{
        val INSERT_PROGRAM_SUCCESS = "Successfully inserted new program."
        val INSERT_PROGRAM_FAILED = "Failed to insert new program."
    }

    abstract suspend fun handleSuccess(resultObj: CacheDataType): DataState<OutputType>?

}