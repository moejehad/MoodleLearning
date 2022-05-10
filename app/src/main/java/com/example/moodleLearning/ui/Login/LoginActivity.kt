package com.example.moodleLearning.ui.Login

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

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
                auth.signInWithEmailAndPassword(binding.etEmail.text.toString(), binding.etPassword.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("login", "LoginUserWithEmail:success")
                            val user = auth.currentUser
                            saveUserSession(applicationContext, user!!)
                            updateUserToken(applicationContext, user.uid)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e("login", "LoginUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}