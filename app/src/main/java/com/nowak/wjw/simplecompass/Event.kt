package com.nowak.wjw.simplecompass

open class Event<out T>(private val content: T, hasBeenHandled: Boolean) {
    constructor(content: T) : this(content, false)

    var hasBeenHandled = hasBeenHandled
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
