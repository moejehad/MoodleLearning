package com.example.moodleLearning.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moodleLearning.adapters.RegisteredCoursesAdapter
import com.example.moodleLearning.databinding.ActivitySingleCategoryBinding
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.utils.Helper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class SingleCategoryActivity : AppCompatActivity() , RegisteredCoursesAdapter.OnClick{
    val TAG = this.javaClass.simpleName
    private lateinit var categoryName : String
    private lateinit var binding: ActivitySingleCategoryBinding
    private lateinit var adapter : RegisteredCoursesAdapter
    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore

    companion object {
        const val CATEGORY_NAME = "categoryName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra(CATEGORY_NAME).toString()

        adapter = RegisteredCoursesAdapter(this, this)
        binding.rvCourses.adapter = adapter

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        db.collection(Course.COURSES_COLLECTION).whereEqualTo(Course.COURSE_CATEGORY, categoryName).get()
            .addOnSuccessListener { docs ->
                val courses = docs.toObjects<Course>()
                adapter.setData(courses)
            }
            .addOnFailureListener {
                Helper.toast(applicationContext, " Failed to get courses in $categoryName")
                finish()
            }
    }

    override fun onClickRegisteredCourse(course: Course) {
        val i = Intent(this, CourseDetailsActivity::class.java)
        i.putExtra(CourseDetailsActivity.EXTRA_COURSE_ID, course.id)
        i.putExtra(CourseDetailsActivity.EXTRA_COURSE_NAME, course.title)
        startActivity(i)
    }
}