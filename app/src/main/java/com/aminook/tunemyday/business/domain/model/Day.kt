package com.aminook.tunemyday.business.domain.model

data class Day (
    val fullName:String,
    val shortName:String,
    val date:String,
    val dayIndex:Int,
    var isChosen:Boolean=false
)