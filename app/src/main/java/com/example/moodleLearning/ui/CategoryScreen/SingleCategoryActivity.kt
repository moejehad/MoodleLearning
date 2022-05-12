package com.example.moodleLearning.ui.CategoryScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moodleLearning.data.adapters.RegisteredCoursesAdapter
import com.example.moodleLearning.databinding.ActivitySingleCategoryBinding
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.ui.CourseDetails.CourseDetailsActivity
import com.example.moodleLearning.utils.Constant.CATEGORY_NAME
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_CATEGORY
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_ID
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_NAME
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra(CATEGORY_NAME).toString()

        binding.screenTitle.text = categoryName
        adapter = RegisteredCoursesAdapter(this, this)
        binding.rvCourses.adapter = adapter

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        db.collection(COURSES_COLLECTION).whereEqualTo(COURSE_CATEGORY, categoryName).get()
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
        i.putExtra(EXTRA_COURSE_ID, course.id)
        i.putExtra(EXTRA_COURSE_NAME, course.title)
        startActivity(i)
    }
}