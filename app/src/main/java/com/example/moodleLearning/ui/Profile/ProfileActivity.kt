package com.example.moodleLearning.ui.Profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moodleLearning.R
import com.example.moodleLearning.data.models.User
import com.example.moodleLearning.utils.Constant.SHARED_USER_ID
import com.example.moodleLearning.utils.Constant.USERS_COLLECTION
import com.example.moodleLearning.utils.Constant.USER_ADDRESS
import com.example.moodleLearning.utils.Constant.USER_BIO
import com.example.moodleLearning.utils.Constant.USER_BIRTHDAY
import com.example.moodleLearning.utils.Constant.USER_EMAIL
import com.example.moodleLearning.utils.Constant.USER_FAMILY_NAME
import com.example.moodleLearning.utils.Constant.USER_FIRST_NAME
import com.example.moodleLearning.utils.Constant.USER_MIDDLE_NAME
import com.example.moodleLearning.utils.Constant.USER_PHONE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser
    lateinit var userObject : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db.collection(USERS_COLLECTION).document(user!!.uid).get().addOnSuccessListener {
            userObject = it.toObject(User::class.java)!!
            username.text = userObject!!.firstName+" "+userObject!!.middleName+" "+userObject!!.lastName
            birthdayYear.text = userObject.birthdayYear.toString()
            userEmail.text = userObject.email
            userPhone.text = userObject.phone.toString()
            userAddress.text = userObject.address
            userBio.text = userObject.bio
        }

        backIcon.setOnClickListener {
            onBackPressed()
        }

        editIcon.setOnClickListener {
            val i = Intent(this,EditProfileActivity::class.java)
            i.putExtra(SHARED_USER_ID,user.uid)
            i.putExtra(USER_FIRST_NAME,userObject.firstName)
            i.putExtra(USER_MIDDLE_NAME,userObject.middleName)
            i.putExtra(USER_FAMILY_NAME,userObject.lastName)
            i.putExtra(USER_BIRTHDAY,userObject.birthdayYear.toString())
            i.putExtra(USER_ADDRESS,userObject.address)
            i.putExtra(USER_PHONE,userObject.phone.toString())
            i.putExtra(USER_EMAIL,userObject.email)
            i.putExtra(USER_BIO,userObject.bio)
            startActivity(i)
        }
    }
}