package com.example.moodleLearning.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

class Lecture {
    @DocumentId
    var id: String = ""
    var title : String = ""
    var docsUrl : String = ""
    var videoUrl : String = ""
    var date : Timestamp = Timestamp.now()
    var watchersIds  = listOf<String>()

    constructor()

    // constructor for firebase
    constructor(title: String, docsUrl: String, vUrl: String, date: Timestamp = Timestamp.now()) {
        this.title = title
        this.docsUrl = docsUrl
        this.videoUrl = vUrl
        this.date = date
    }

    // for adapter
    constructor(id: String, title: String, docsUrl: String, vUrl: String, watchersIds: List<String>, date: Timestamp = Timestamp.now()) {
        this.id = id
        this.title = title
        this.docsUrl = docsUrl
        this.videoUrl = vUrl
        this.watchersIds = watchersIds
        this.date = date
    }

    companion object {
        const val LECTURE_ID = "id"
        const val LECTURE_TITLE = "title"
    }
}