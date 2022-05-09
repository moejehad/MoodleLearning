package com.example.moodleLearning.models

class Message {
    var id: String? = null
    var messengerId: String? = null
    var text: String? = null
    var name: String? = null
    var imageUrl: String? = null

    constructor()
    constructor( messengerId: String?, text: String?, name: String?, imageUrl: String?) {
        this.messengerId = messengerId
        this.text = text
        this.name = name
        this.imageUrl = imageUrl
    }
}