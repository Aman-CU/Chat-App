package com.example.chatapp.model

class Message {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Long? = null
    var currentTime: String? = null

    constructor(){}

    constructor(message: String?, senderId: String?, timestamp: Long?, currentTime: String?){
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
        this.currentTime = currentTime
    }
}