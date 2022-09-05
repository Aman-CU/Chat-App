package com.example.chatapp.model

class Groups {

    var groupId: String? = null
    var groupTitle: String? = null
    var groupDescription: String? = null
    var groupIcon: String? = null
    var timestamp: String? = null
    var createdBy: String? = null

    constructor(){}

    constructor(groupId: String?, groupTitle: String?, groupDescription: String?, groupIcon: String?, timestamp: String?, createdBy: String?){
        this.groupId = groupId
        this.groupTitle = groupTitle
        this.groupDescription = groupDescription
        this.groupIcon = groupIcon
        this.timestamp = timestamp
        this.createdBy = createdBy
    }

}