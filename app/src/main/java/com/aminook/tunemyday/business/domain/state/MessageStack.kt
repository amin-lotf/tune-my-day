package com.aminook.tunemyday.business.domain.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel
import java.lang.IndexOutOfBoundsException


class MessageStack:ArrayList<StateMessage>() {


    @IgnoredOnParcel
    private val _stateMessage=MutableLiveData<StateMessage>()

    @IgnoredOnParcel
    val stateMessage:LiveData<StateMessage>
    get() = _stateMessage


    override fun add(element: StateMessage): Boolean {
        if(this.contains(element)){
            return false
        }
        val transaction=super.add(element)

        if(size==1){
            setStateMessage(element)
        }
        return transaction
    }

    override fun addAll(elements: Collection<StateMessage>): Boolean {
        for (element in elements){
            add(element)
        }

        return true
    }

    override fun removeAt(index: Int): StateMessage {
        try {
            val transaction=super.removeAt(index)
            if(this.isNotEmpty()){
                setStateMessage(this[0])
            }else{
                setStateMessage(null)
            }
            return transaction
        }catch (e:IndexOutOfBoundsException){
            setStateMessage(null)

            return StateMessage(
                Response(
                    message = "",
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.None
                )
            )
        }
    }

    private fun setStateMessage(stateMessage: StateMessage?){
        _stateMessage.value = stateMessage
    }


}