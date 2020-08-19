package com.aminook.tunemyday.business.data.cache

import com.aminook.tunemyday.business.data.util.ErrorConstants.CACHE_DATA_NULL
import com.aminook.tunemyday.business.domain.state.*

abstract class CacheResponseHandler<ViewState,Data>(
    private val response:CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {

    suspend fun getResult():DataState<ViewState>?{
        return when(response){
            is CacheResult.Success -> {
               if (response.value==null){
                   DataState.error(
                       response = Response(
                           message = "${stateEvent?.errorInfo}\n\nReason: ${CACHE_DATA_NULL}.",
                           uiComponentType = UIComponentType.Dialog,
                           messageType = MessageType.Error
                       ),
                       stateEvent=stateEvent
                   )
               }else{
                   handleSuccess(response.value)
               }
            }
            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo}\n\nReason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    ),
                    stateEvent=stateEvent
                )
            }
        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?

}