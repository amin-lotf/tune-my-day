package com.aminook.tunemyday.business.domain.state


import android.view.View
import com.aminook.tunemyday.util.TodoCallback

data class StateMessage(val response: Response)

data class Response(
    val message: String?,
    val uiComponentType: UIComponentType,
    val messageType: MessageType
)

sealed class UIComponentType{

    object Toast: UIComponentType()
    object Dialog: UIComponentType()
    object None: UIComponentType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ): UIComponentType()

    class SnackBar(
        val undoCallback: SnackbarUndoCallback? = null,
        val onDismissCallback: TodoCallback? = null
    ): UIComponentType()
}

sealed class MessageType{
    object Success:MessageType()
    object Error:MessageType()
    object Info:MessageType()
    object None:MessageType()

}

interface AreYouSureCallback {
    fun proceed()
    fun cancel()
}

interface SnackbarUndoCallback {
    fun undo()
}

class SnackbarUndoListener
constructor(
    private val snackbarUndoCallback: SnackbarUndoCallback?
): View.OnClickListener {
    override fun onClick(v: View?) {
        snackbarUndoCallback?.undo()
    }
}
