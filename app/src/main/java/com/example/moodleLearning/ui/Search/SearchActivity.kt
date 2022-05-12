package com.example.moodleLearning.ui.Search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.SearchView
import com.example.moodleLearning.data.adapters.RegisteredCoursesAdapter
import com.example.moodleLearning.databinding.ActivitySearchBinding
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.ui.CourseDetails.CourseDetailsActivity
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_TITLE
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_ID
import com.example.moodleLearning.utils.Helper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class SearchActivity : AppCompatActivity(), RegisteredCoursesAdapter.OnClick {
    val TAG = this.javaClass.simpleName
    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: RegisteredCoursesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        adapter = RegisteredCoursesAdapter(this, this)
        binding.rvCourses.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 1) {
                    db.collection(COURSES_COLLECTION)
                        .orderBy(COURSE_TITLE)
                        .whereGreaterThanOrEqualTo(COURSE_TITLE, binding.etSearch.text.toString())
                        .get()
                        .addOnSuccessListener { docs ->
                            val courses = docs.toObjects<Course>()
                            adapter.setData(courses)
                        }
                        .addOnFailureListener {
                            Helper.toast(applicationContext, " Failed to get courses in ${binding.etSearch.text}")
                            finish()
                        }
                }
            }
        })
    }

    override fun onClickRegisteredCourse(course: Course) {
        val i = Intent(this, CourseDetailsActivity::class.java)
        i.putExtra(EXTRA_COURSE_ID, course.id)
        startActivity(i)
    }
}