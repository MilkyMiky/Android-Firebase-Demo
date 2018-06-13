package com.example.user.firebaseapp

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue
import java.util.*

/**
 * Created by Mikhail Lysyansky on 13.06.18.
 */

@IgnoreExtraProperties
class Message {

    var user: String = ""
    var messageText: String = ""
    var date: Long = 0

    constructor(user: String, text: String) {
        this.user = user
        this.messageText = text
    }

    constructor()

    fun createMessageObj(): Map<String, Any> {
        val messageObj = HashMap<String, Any>()
        messageObj["date"] = ServerValue.TIMESTAMP
        messageObj["messageText"] = this.messageText
        messageObj["user"] = this.user
        return messageObj
    }
}