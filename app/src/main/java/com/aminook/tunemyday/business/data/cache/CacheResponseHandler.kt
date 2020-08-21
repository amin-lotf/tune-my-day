package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_DATA_NULL
import com.aminook.tunemyday.business.domain.state.*

abstract class CacheResponseHandler<Input,Output>(
    private val response:CacheResult<Input?>
) {

    suspend fun getResult():DataState<Output?>{
        return when(response){
            is CacheResult.Success -> {
               if (response.value==null){
                   DataState.error(
                       response = Response(
                           message = CACHE_DATA_NULL,
                           uiComponentType = UIComponentType.Dialog,
                           messageType = MessageType.Error
                       )
                   )
               }else{
                   handleSuccess(response.value)
               }
            }
            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = response.errorMessage,
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                )
            }
        }
    }

    abstract suspend fun handleSuccess(resultObj: Input): DataState<Output?>

}