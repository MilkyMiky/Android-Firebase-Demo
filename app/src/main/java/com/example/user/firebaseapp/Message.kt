package com.example.user.firebaseapp

import java.util.*

/**
 * Created by Mikhail Lysyansky on 13.06.18.
 */
data class Message(var user: String, var text: String, var date: Date) {

    constructor (map: Map<String, Any>) : this(
        map["user"].toString(),
        map["messageText"].toString(),
        map["date"] as Date
    )

    fun createMessageObj(): Map<String, Any> {
        val messageObj = HashMap<String, Any>()
        messageObj["date"] = this.date
        messageObj["messageText"] = this.text
        messageObj["user"] = this.user
        return messageObj
    }
}