package com.aminook.tunemyday.business.domain.state

interface StateEvent {

    val errorInfo:String

    val eventName:String

    val displayProgressBar:Boolean
}