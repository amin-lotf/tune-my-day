package com.aminook.tunemyday.framework.presentation.common

import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.util.ROUTINE_INDEX
import com.aminook.tunemyday.util.SCREEN_BLANK
import com.aminook.tunemyday.util.SCREEN_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    val dataStoreCache: DataStore<Preferences>,
    val dataStoreSettings: DataStore<Preferences>
) : ViewModel() {


    var routineId:Long=0
    private var _stateMessage = MutableLiveData<Event<StateMessage?>>()
    //private val TAG="aminjoon"
    val stateMessage: LiveData<Event<StateMessage?>>
        get() = _stateMessage

    protected suspend fun processResponse(event: Event<StateMessage?>?) {

        withContext(Main) {
            event?.let {
                _stateMessage.postValue(it)
            }

        }
    }
    fun getRoutineIndex(): LiveData<Long> {
        return dataStoreCache.data
            .map {
                routineId=it[ROUTINE_INDEX]?:0
                routineId

            }.asLiveData()
    }

    fun getScreenType(): LiveData<String> {
        return dataStoreCache.data
            .map { cache->
                cache[SCREEN_TYPE]?: SCREEN_BLANK
            }.asLiveData()
    }

    fun setScreenType(type:String){
        CoroutineScope(Default).launch {
            dataStoreCache.edit { settings->
                settings[SCREEN_TYPE]=type
            }
        }
    }

    protected fun getConfirmation(message: String,areYouSureCallback: AreYouSureCallback){
        _stateMessage.value= Event(
            StateMessage(
                Response(
                    message=message,
                    uiComponentType = UIComponentType.AreYouSureDialog(areYouSureCallback),
                    messageType = MessageType.Info
                )
            )
        )
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