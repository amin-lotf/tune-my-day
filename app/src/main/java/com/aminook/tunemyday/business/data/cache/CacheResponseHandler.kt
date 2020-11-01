package com.aminook.tunemyday.business.data.cache


import com.aminook.tunemyday.business.domain.state.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map



abstract class CacheResponseHandler<CacheDataType,OutputType>(
) {

     fun getResult(cacheCall: () -> Flow<CacheDataType?>): Flow<DataState<OutputType>?> {
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
            FirebaseCrashlytics.getInstance().recordException(throwable)
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
    abstract  fun handleSuccess(resultObj: CacheDataType): DataState<OutputType>?
    companion object{
        const val CACHE_ERROR_UNKNOWN = "Unknown cache error"
        const val CACHE_DATA_NULL = "Error catching data"
    }

}