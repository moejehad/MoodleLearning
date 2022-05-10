package com.example.moodleLearning.data.models

import com.google.firebase.firestore.DocumentId

class User {
    @DocumentId
    var id: String = ""
    var firstName: String = ""
    var middleName: String = ""
    var lastName: String = ""
    var birthdayYear: Int = 1900
    var address: String = ""
    var phone: Long = 0L
    var email: String = ""
    var bio: String = ""
    var role: Int = 0
    var token: String = ""
    var activeCourses : List<String>? = listOf()
    var finishedCourses : List<String>? = listOf()

    constructor()
    constructor(id: String, firstName: String, middleName: String, lastName: String, birthdayYear: Int, address: String, phone: Long, email: String, bio: String, role: Int, token: String, activeCourses: List<String>?, finishedCourses: List<String>?) {
        this.id = id
        this.firstName = firstName
        this.middleName = middleName
        this.lastName = lastName
        this.birthdayYear = birthdayYear
        this.address = address
        this.phone = phone
        this.email = email
        this.bio = bio
        this.role = role
        this.token = token
        this.activeCourses = activeCourses
        this.finishedCourses = finishedCourses
    }

    constructor(id: String, fName: String, middleName: String, lName: String, email: String) {
        this.id = id
        this.firstName = fName
        this.middleName = middleName
        this.lastName = lName
        this.email = email
    }

}