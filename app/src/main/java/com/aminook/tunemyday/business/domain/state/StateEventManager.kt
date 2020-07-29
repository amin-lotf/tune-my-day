package com.aminook.tunemyday.business.domain.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * - Keeps track of active StateEvents in DataChannelManager
 * - Keeps track of whether the progress bar should show or not based on a boolean
 *      value in each StateEvent (shouldDisplayProgressBar)
 */
class StateEventManager {

    private val activeStateEvents = HashMap<String, StateEvent>()

    private val _displayProgressBar = MutableLiveData<Boolean>()

    val displayProgressBar: LiveData<Boolean>
        get() = _displayProgressBar

    fun getActiveJobsNames()=
        activeStateEvents.keys

    fun clearActiveStateEventCounter(){
        activeStateEvents.clear()
        syncNumActiveStateEvents()
    }

    fun addStateEvent(stateEvent: StateEvent){
        activeStateEvents[stateEvent.eventName]=stateEvent
        syncNumActiveStateEvents()
    }

    fun removeStateEvent(stateEvent: StateEvent?){
        stateEvent?.let {
            activeStateEvents.remove(it.eventName)
            syncNumActiveStateEvents()
        }
    }

    fun isStateEventActive(stateEvent: StateEvent)=
        activeStateEvents.containsKey(stateEvent.eventName)


    private fun syncNumActiveStateEvents(){
        var displayProgressBar=false

        for(stateEvent in activeStateEvents.values){
            if(stateEvent.displayProgressBar){
                displayProgressBar=true
            }
        }
        _displayProgressBar.value=displayProgressBar
    }
}