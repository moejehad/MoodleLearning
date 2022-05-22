package com.example.moodleLearning.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.moodleLearning.data.models.User
import com.example.moodleLearning.utils.Constant.SHARED_PREF_NAME
import com.example.moodleLearning.utils.Constant.SHARED_USER_EMAIL
import com.example.moodleLearning.utils.Constant.SHARED_USER_ID
import com.example.moodleLearning.utils.Constant.SHARED_USER_PHOTO
import com.example.moodleLearning.utils.Constant.USERS_COLLECTION
import com.example.moodleLearning.utils.Constant.USER_TOKEN
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat


class Helper {
    companion object{

        fun log(tag: Context, message: String) {
            Log.e(tag.javaClass.simpleName, message)
        }

        fun toast(context: Context, message: String){
            Toast.makeText(context, "$message", Toast.LENGTH_LONG).show()
        }

        fun formatDate(date: Timestamp): String {
            val dateArray = SimpleDateFormat("dd/MM/yyyy").format(date.toDate()).split("/")
            return "${dateArray[2]}/${dateArray[1]}/${dateArray[0]}"
        }

        fun openUrl(context: Context, url: String){
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }


        fun getCurrentUser(): FirebaseUser? {
            return Firebase.auth.currentUser
        }

        fun subscribeToTopic(topic: String){
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/$topic")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscription failed"
                    }
                    Log.e("Firebase", msg)
                }
        }

        fun unSubscribeToTopic(topic: String){
            FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/$topic")
                .addOnCompleteListener { task ->
                    var msg = "UnSubscribed"
                    if (!task.isSuccessful) {
                        msg = "UnSubscription failed"
                    }
                    Log.e("Firebase", msg)
                }
        }

        fun saveUserSession(context: Context, user: FirebaseUser) {
            val sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(SHARED_USER_ID, user.uid)
                putString(SHARED_PREF_NAME, user.displayName)
                putString(SHARED_USER_EMAIL, user.email)
                putString(SHARED_USER_PHOTO, user.photoUrl?.toString())
                commit()
            }
        }

        fun updateUserToken(context: Context, userId: String) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Firebase.firestore.collection(USERS_COLLECTION).document(userId).update(USER_TOKEN,token)
                val sharedPref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(USER_TOKEN, token)
                    apply()
                }
            }
        }

        fun fillImage(context: Context, url: String, imageView: ImageView) {
            Glide.with(context)
                .load(url)
                .placeholder(com.google.firebase.inappmessaging.display.R.drawable.image_placeholder)
                .fitCenter()
                .into(imageView)
        }
    }
}