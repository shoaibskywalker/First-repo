package com.test.loginfirebase.data

import java.util.Date

class Message {

     var message: String? = null
    var senderId: String? = null
    var timeStamp: Long? = null
    var messageId: String = ""


    constructor() {}

    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
        this.timeStamp = Date().time
        var messageId: String = ""


    }

}
