package com.example.moodleLearning.ui.Profile

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.example.moodleLearning.utils.Constant.USER_ID
import com.example.moodleLearning.utils.Constant.USER_MIDDLE_NAME
import com.example.moodleLearning.utils.Constant.USER_PHONE
import com.example.moodleLearning.utils.Helper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_profile.backIcon

class EditProfileActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    lateinit var userId: String
    lateinit var firstName: String
    lateinit var middleName: String
    lateinit var lastName: String
    lateinit var birthdayYear: String
    lateinit var userEmail: String
    lateinit var userPhone: String
    lateinit var userAddress: String
    lateinit var userBio: String
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Update Profile")
        progressDialog.setCancelable(false)

        backIcon.setOnClickListener {
            onBackPressed()
        }

        try {
            getUserData()
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }

        btnEdit.setOnClickListener {
            if (firstName.isNotEmpty() && middleName.isNotEmpty() && lastName.isNotEmpty()
                && userEmail.isNotEmpty() && userBio.isNotEmpty() && birthdayYear.isNotEmpty()
                && userAddress.isNotEmpty() && userPhone.isNotEmpty()
            ) {
                UpdateUser(
                    userId,
                    firstName,
                    middleName,
                    lastName,
                    userEmail,
                    userBio,
                    birthdayYear,
                    userAddress,
                    userPhone
                )
            } else {
                Helper.toast(applicationContext, "Check Your Inputs Please")
            }
        }

    }

    private fun UpdateUser(
        id: String,
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        bio: String,
        birthdayYear: String,
        address: String,
        phone: String
    ) {
        var user = mapOf<String, String>(
            "id" to id,
            "firstName" to EditFirstName.text.toString(),
            "middleName" to EditMiddleName.text.toString(),
            "lastName" to EditLastName.text.toString(),
            "email" to EditEmail.text.toString(),
            "bio" to EditBio.text.toString(),
            "birthdayYear" to EditBirthday.text.toString(),
            "address" to EditAddress.text.toString(),
            "phone" to EditPhone.text.toString()
        )
        progressDialog.show()

        db.collection(USERS_COLLECTION)
            .document(id)
            .update(user)
            .addOnSuccessListener {
                Helper.toast(applicationContext, "User Updated Successfully")
                finish()
                progressDialog.dismiss()
            }.addOnFailureListener {
                it.printStackTrace()
                Helper.toast(applicationContext, "Failed to Updated User ${it.message}")
                progressDialog.dismiss()
            }
        progressDialog.dismiss()
    }

    private fun getUserData() {

        userId = intent.getStringExtra(SHARED_USER_ID)!!
        firstName = intent.getStringExtra(USER_FIRST_NAME)!!
        middleName = intent.getStringExtra(USER_MIDDLE_NAME)!!
        lastName = intent.getStringExtra(USER_FAMILY_NAME)!!
        userEmail = intent.getStringExtra(USER_BIRTHDAY)!!
        userBio = intent.getStringExtra(USER_ADDRESS)!!
        birthdayYear = intent.getStringExtra(USER_PHONE)!!
        userAddress = intent.getStringExtra(USER_EMAIL)!!
        userPhone = intent.getStringExtra(USER_BIO)!!

        EditFirstName.setText(firstName)
        EditMiddleName.setText(middleName)
        EditLastName.setText(lastName)
        EditEmail.setText(userEmail)
        EditBio.setText(userBio)
        EditBirthday.setText(birthdayYear)
        EditAddress.setText(userAddress)
        EditPhone.setText(userPhone)
    }
}