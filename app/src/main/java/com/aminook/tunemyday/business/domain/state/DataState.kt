package com.aminook.tunemyday.business.domain.state

data class DataState<T>(
    var stateMessage: Event<StateMessage>? = null,
    var data: T? = null
) {

    companion object {

        fun <T> error(
            response: Response
        ): DataState<T> {
            return DataState(
                stateMessage = Event(
                    StateMessage(
                        response
                    )
                ),
                data = null
            )
        }

        fun <T> data(
            response: Response?,
            data: T? = null
        ): DataState<T> {
            return DataState(
                stateMessage = response?.let {
                    Event(
                        StateMessage(
                            it
                        )
                    )
                },
                data = data
            )
        }
    }
}


open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}