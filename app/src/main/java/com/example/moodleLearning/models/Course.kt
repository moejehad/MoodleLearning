package com.example.moodleLearning.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

class Course {
    @DocumentId
    var id: String = ""
    var img: String = ""
    var title: String = ""
    var category: String = ""
    var registersIds = listOf<String>()
    var registersEmails = listOf<String>()
    var desc: String = ""
    var ownerId: String = ""
    var hours: Long = 0
    var createDate: Timestamp = Timestamp.now()
    var lastUpdateDate: Timestamp = Timestamp.now()

    // Firestore requires a no-arg constructor
    constructor()

    // constructor for creating a new course
    constructor(img: String, title: String, category: String, desc: String, ownerId: String, hours: Long) {
        this.img = img
        this.title = title
        this.category = category
        this.desc = desc
        this.ownerId = ownerId
        this.hours = hours
    }

    // Most Sell Adapter Constructor
    constructor(id: String,img: String, title: String, category: String, hours: Long) {
        this.id = id
        this.img = img
        this.title = title
        this.category = category
        this.hours = hours
    }

    companion object {
        const val COURSES_COLLECTION = "COURSES"

        const val COURSE_ID = "id"
        const val COURSE_IMG = "img"
        const val COURSE_TITLE = "title"
        const val COURSE_CATEGORY = "category"
        const val COURSE_DESC = "desc"
        const val COURSE_OWNER_ID = "ownerId"
        const val COURSE_HOURS = "hours"
        const val COURSE_REGISTERS_IDS = "registersIds"
        const val COURSE_REGISTERS_EMAILS = "registersEmails"
        const val COURSE_LAST_UPDATE_DATE = "lastUpdateDate"
        const val COURSE_CREATE_DATE = "createDate"
    }
}