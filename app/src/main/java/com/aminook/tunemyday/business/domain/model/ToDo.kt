package com.aminook.tunemyday.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ToDo(
    val id: String,
    val title: String,
    val isDone: Boolean,
    val isImportant: Boolean
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (this.javaClass != other?.javaClass) return false

        other as ToDo

        return this.id == other.id &&
                this.title == other.title &&
                this.isDone == other.isDone &&
                this.isImportant == other.isImportant

    }

    override fun hashCode(): Int {
        var result=id.hashCode()
        result=31*result+title.hashCode()
        result=31*result+isDone.hashCode()
        result=31*result+isImportant.hashCode()

        return result
    }
}