package com.example.moodleLearning.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

class Assignment {
    @DocumentId
    var id : String = ""
    var date: Timestamp = Timestamp.now()
    var fileUrl: String = ""
    var userId: String = ""
    @Exclude var userName: String? = null
    @Exclude var userEmail: String? = null

    constructor()
    constructor(id: String, fileUrl: String, userId: String,  date: Timestamp = Timestamp.now()) {
        this.id = id
        this.date = date
        this.fileUrl = fileUrl
        this.userId = userId
    }

    // assignment adapter constructor
    constructor(id: String, url: String, date: Timestamp, userName: String, userEmail: String) {
        this.id = id
        this.date = date
        this.fileUrl = url
        this.userName = userName
        this.userEmail = userEmail
    }

    companion object {
        const val COLLECTION_NAME = "Assignments"

        const val ASSIGNMENT_ID = "id"
        const val ASSIGNMENT_DATE = "date"
        const val ASSIGNMENT_FILE_URL = "fileUrl"
        const val ASSIGNMENT_USER_ID = "userId"
    }
}