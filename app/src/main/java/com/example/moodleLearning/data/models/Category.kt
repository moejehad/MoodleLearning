package com.example.moodleLearning.data.models

import com.google.firebase.firestore.DocumentId

class Category {
    @DocumentId
    var id: String = ""
    var name: String = ""

    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }

}