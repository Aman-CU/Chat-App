

class GroupMessageModel {
    var message: String? = null
    var sender: String? = null
    var timestamp: String? = null
    var type: String? = null

    constructor(){}

    constructor(message: String?, sender: String?, timestamp: String?, type: String?){
        this.message = message
        this.sender = sender
        this.timestamp = timestamp
        this.type = type
    }
}
