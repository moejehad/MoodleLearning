package com.example.moodleLearning.ui.CreateAccount

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.moodleLearning.databinding.ActivitySignupBinding
import com.example.moodleLearning.data.models.User
import com.example.moodleLearning.ui.MainActivity
import com.example.moodleLearning.utils.Constant.STUDENT_ENUM
import com.example.moodleLearning.utils.Constant.TEACHER_ENUM
import com.example.moodleLearning.utils.Constant.USERS_COLLECTION
import com.example.moodleLearning.utils.Helper.Companion.saveUserSession
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private var token = ""
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Create Account")
        progressDialog.setCancelable(false)

        auth = Firebase.auth
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            this.token = token
            Log.d("TOKEN", token)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            binding.btnRegister.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        binding.tvLogin.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            if (binding.etFirstName.text.isNotEmpty() &&
                binding.etLastName.text.isNotEmpty()&&
                binding.etFamilyName.text.isNotEmpty()&&
                binding.etEmail.text.isNotEmpty()&&
                binding.etBirthday.text.isNotEmpty()&&
                binding.etAddress.text.isNotEmpty()&&
                binding.etEmail.text.isNotEmpty()&&
                binding.etPhone.text.isNotEmpty()&&
                binding.etPassword.text.isNotEmpty()&&
                binding.etConfirmPassword.text.isNotEmpty()&&
                binding.etBio.text.isNotEmpty()&&
                binding.etPassword.text.toString() == binding.etConfirmPassword.text.toString()
            ) {
                progressDialog.show()
                auth.createUserWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            saveUserDate(user!!,
                                binding.etFirstName.text.toString(),
                                binding.etLastName.text.toString(),
                                binding.etFamilyName.text.toString(),
                                binding.etBirthday.text.toString().toInt(),
                                binding.etAddress.text.toString(),
                                binding.etEmail.text.toString(),
                                binding.etBio.text.toString(),
                                binding.etPhone.text.toString().toLong(),
                                if (binding.rbUser.isChecked) STUDENT_ENUM else TEACHER_ENUM,
                                token
                            )
                            progressDialog.dismiss()
                        } else {
                            toast(baseContext, "Authentication failed. ${task.exception?.message}")
                        }
                        progressDialog.dismiss()
                    }
            } else {
                toast(applicationContext, "Should Fill All Fields")
            }
        }
    }

    private fun saveUserDate(user: FirebaseUser, fName: String, mName: String, lName: String, birthdayYear: Int, address: String, email: String, bio: String, phone: Long,role: Int,token: String) {
        val cUser = User(user.uid,fName,mName,lName,birthdayYear,address,phone,email,bio,role,token,null,null)
        db.collection(USERS_COLLECTION).document(user.uid).set(cUser).addOnSuccessListener {
            val profileUpdates = userProfileChangeRequest {
                displayName = "$fName $lName"
            }
            user.updateProfile(profileUpdates)
            saveUserSession(applicationContext, user)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}