package com.example.chatapp.model

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? =  null
    var status: String? = null

    constructor(){}

    constructor(name: String?, email:String?, uid: String?){
        this.name = name
        this.email = email
        this.uid = uid
    }

    constructor(name: String?, email:String?, uid: String?, status: String?){
        this.name = name
        this.email = email
        this.uid = uid
        this.status = status
    }
}