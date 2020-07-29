package com.aminook.tunemyday.business.domain.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

abstract class DataChannelManager<ViewState> {

    private var channelScope: CoroutineScope? = null
    private val stateEventManager = StateEventManager()

    val messageStack = MessageStack()

    val displayProgressBar
        get() = stateEventManager.displayProgressBar

    fun setupChannel(){
        cancelJobs()
    }


    fun launchJob(
        stateEvent: StateEvent,
        jobFunction:Flow<DataState<ViewState>?>
    ){
        if(canExecuteNewStateEvent(stateEvent)){
            addStateEvent(stateEvent)
            jobFunction.onEach { dataState ->
                dataState?.let { dState ->
                        withContext(Main) {
                            dState.data?.let { data->
                                handleNewData(data)
                            }

                            dState.stateMessage?.let { stateMessage ->
                                handleNewStateMessage(stateMessage)
                            }

                            dState.stateEvent?.let { sEvent ->
                                removeStateEvent(sEvent)
                            }
                        }

                }
            }.launchIn(getChannelScope())
        }
    }

    private fun canExecuteNewStateEvent(stateEvent: StateEvent):Boolean{
        if(isJobAlreadyActive(stateEvent) || messageStack.isNotEmpty()){
            return false
        }
        return true
    }


    private fun handleNewStateMessage(stateMessage: StateMessage){
        messageStack.add(stateMessage)
    }

    // for debugging
    fun getActiveJobs() = stateEventManager.getActiveJobsNames()

    fun clearActiveStateEventCounter()
            = stateEventManager.clearActiveStateEventCounter()

    fun addStateEvent(stateEvent: StateEvent){
        stateEventManager.addStateEvent(stateEvent)
    }


    fun removeStateEvent(stateEvent: StateEvent?){
        stateEventManager.removeStateEvent(stateEvent)
    }

    fun isJobAlreadyActive(stateEvent: StateEvent)=
        stateEventManager.isStateEventActive(stateEvent)


    fun getChannelScope():CoroutineScope=
        channelScope?: setupNewChannelScope(CoroutineScope(IO))

    fun setupNewChannelScope(coroutineScope: CoroutineScope):CoroutineScope{
        channelScope=coroutineScope
        return coroutineScope
    }

    fun cancelJobs(){
        if(channelScope?.isActive==true){
            channelScope?.cancel()
        }
        clearActiveStateEventCounter()
    }

    abstract fun handleNewData(data: ViewState)
}