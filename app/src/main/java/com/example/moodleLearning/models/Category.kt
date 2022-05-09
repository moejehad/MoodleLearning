package com.example.moodleLearning.models

import com.google.firebase.firestore.DocumentId

class Category {
    @DocumentId
    var id: String = ""
    var name: String = ""

    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }
    constructor()

    companion object {
        const val CATEGORIES_COLLECTION = "CATEGORIES"

        const val CATEGORIES_NAME = "name"
    }
}