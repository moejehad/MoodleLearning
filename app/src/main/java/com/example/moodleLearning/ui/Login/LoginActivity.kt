package com.example.moodleLearning.ui.Login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.moodleLearning.utils.Helper.Companion.saveUserSession
import com.example.moodleLearning.utils.Helper.Companion.updateUserToken
import com.example.moodleLearning.databinding.ActivityLoginBinding
import com.example.moodleLearning.ui.MainActivity
import com.example.moodleLearning.ui.CreateAccount.SignupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Login")
        progressDialog.setCancelable(false)

        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            binding.btnLogin.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            if (binding.etEmail.text.isNotEmpty() && binding.etPassword.text.isNotEmpty()) {
                progressDialog.show()
                auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            saveUserSession(applicationContext, user!!)
                            updateUserToken(applicationContext, user.uid)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            progressDialog.dismiss()
                        } else {
                            Toast.makeText(baseContext, "Authentication failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                        progressDialog.dismiss()
                    }
            }
        }
    }
}