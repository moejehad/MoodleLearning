package com.example.moodleLearning.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.moodleLearning.adapters.TeacherCoursesAdapter
import com.example.moodleLearning.databinding.ActivityDashboardBinding
import com.example.moodleLearning.models.Course
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity(), TeacherCoursesAdapter.OnClick{
    val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityDashboardBinding
    private val db = Firebase.firestore
    private lateinit var adapter: TeacherCoursesAdapter
    private var teacherId = ""

    companion object {
        const val EXTRA_TEACHER_ID = "teacherId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        teacherId = intent.getStringExtra(EXTRA_TEACHER_ID).toString()

        adapter = TeacherCoursesAdapter(this, this)
        binding.rvCourses.adapter = adapter

        binding.btnAddCourse.setOnClickListener {
            val intent = Intent(this, AddCourseActivity::class.java)
            intent.putExtra(AddCourseActivity.EXTRA_TEACHER_ID, teacherId)
            startActivity(intent)
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        getTeacherCourses()
    }

    private fun getTeacherCourses() {
        db.collection(Course.COURSES_COLLECTION)
            .whereEqualTo(Course.COURSE_OWNER_ID, teacherId)
            .get()
            .addOnSuccessListener {
                val courses = it.toObjects(Course::class.java)
                adapter.setData(courses)
            }
    }

    override fun onClickCourse(course: Course) {
        val i = Intent(this, CourseDetailsActivity::class.java)
        i.putExtra(CourseDetailsActivity.EXTRA_COURSE_ID, course.id)
        i.putExtra(CourseDetailsActivity.EXTRA_COURSE_NAME, course.title)
        i.putExtra(CourseDetailsActivity.EXTRA_IS_TEACHER, true)
        startActivity(i)
    }

    override fun onClickDeleteCourse(course: Course) {
        AlertDialog.Builder(this).apply {
            setTitle("Delete Course")
            setMessage("Are you sure you want to delete this course?")
            setPositiveButton("Yes, Delete Course") { _, _ ->
                db.collection(Course.COURSES_COLLECTION)
                    .document(course.id)
                    .delete()
                    .addOnSuccessListener {
                        adapter.clearData()
                        getTeacherCourses()
                    }
            }
            setNegativeButton("No, Cancel") { _, _ -> }
            show()
        }
    }

    override fun onClickEditCourse(course: Course) {
        val i = Intent(this, AddCourseActivity::class.java)
        i.putExtra(AddCourseActivity.EXTRA_TEACHER_ID, teacherId)
        i.putExtra(AddCourseActivity.EXTRA_COURSE_ID, course.id)
        i.putExtra(AddCourseActivity.EXTRA_COURSE_NAME, course.title)
        startActivity(i)
    }
}