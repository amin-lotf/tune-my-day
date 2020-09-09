package com.aminook.tunemyday.framework.presentation.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aminook.tunemyday.business.domain.state.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {
    private var _stateMessage = MutableLiveData<Event<StateMessage?>>()
    private val TAG="aminjoon"
    val stateMessage: LiveData<Event<StateMessage?>>
        get() = _stateMessage

    protected suspend fun processResponse(event: Event<StateMessage?>?) {

        withContext(Main) {
            Log.d(TAG, "processResponse: ${event?.peekContent()?.response?.message}")
            _stateMessage.postValue( event)
        }
    }

    protected fun handleLocalError(message:String){
        _stateMessage.value= Event(
            StateMessage(
                Response(
                    message=message,
                    uiComponentType = UIComponentType.Toast,
                    messageType = MessageType.Info
                )
            )
        )
    }
}