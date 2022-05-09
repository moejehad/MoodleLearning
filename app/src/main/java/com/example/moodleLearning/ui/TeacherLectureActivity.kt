package com.example.moodleLearning.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moodleLearning.databinding.ActivityTeacherLectureBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TeacherLectureActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityTeacherLectureBinding
    private val db = Firebase.firestore
    private var teacherId = ""
    private var courseId : String? = null

    companion object {
        const val EXTRA_TEACHER_ID = "teacherId"
        const val EXTRA_COURSE_ID = "courseId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherLectureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherId = intent.getStringExtra(EXTRA_TEACHER_ID).toString()
        courseId = intent.getStringExtra(EXTRA_COURSE_ID) // if null, then add new course else update existing course
    }
}