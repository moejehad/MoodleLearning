package com.example.moodleLearning.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.moodleLearning.data.adapters.CategoriesAdapter
import com.example.moodleLearning.data.adapters.CoursesAdapter
import com.example.moodleLearning.data.adapters.RegisteredCoursesAdapter
import com.example.moodleLearning.databinding.ActivityMainBinding
import com.example.moodleLearning.data.models.Category
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.data.models.User
import com.example.moodleLearning.ui.CategoryScreen.SingleCategoryActivity
import com.example.moodleLearning.ui.CourseDetails.CourseDetailsActivity
import com.example.moodleLearning.ui.Profile.ProfileActivity
import com.example.moodleLearning.ui.Search.SearchActivity
import com.example.moodleLearning.ui.Teacher.MyCourses.DashboardActivity
import com.example.moodleLearning.utils.Constant.CATEGORIES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_CATEGORY
import com.example.moodleLearning.utils.Constant.COURSE_HOURS
import com.example.moodleLearning.utils.Constant.COURSE_IMG
import com.example.moodleLearning.utils.Constant.COURSE_TITLE
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_ID
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_NAME
import com.example.moodleLearning.utils.Constant.TEACHER_ENUM
import com.example.moodleLearning.utils.Constant.USERS_COLLECTION
import com.example.moodleLearning.utils.Constant.USER_ACTIVE_COURSES
import com.example.moodleLearning.utils.Helper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() , CoursesAdapter.OnClick, RegisteredCoursesAdapter.OnClick, CategoriesAdapter.OnClick{
    val TAG = "MainActivity"
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private lateinit var binding: ActivityMainBinding
    private lateinit var coursesAdapter: CoursesAdapter
    private lateinit var registeredCoursesAdapter: RegisteredCoursesAdapter
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coursesAdapter = CoursesAdapter(this, this)
        binding.rvCourses.adapter = coursesAdapter

        registeredCoursesAdapter = RegisteredCoursesAdapter(this, this)
        binding.rvRegisteredCourses.adapter = registeredCoursesAdapter

        categoriesAdapter = CategoriesAdapter(this, this)
        binding.rvCategories.adapter = categoriesAdapter

        binding.userProfile.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            startActivity(i)
        }

        db.collection(USERS_COLLECTION).document(user!!.uid).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            if(user!!.role == TEACHER_ENUM){
                binding.btnDashboard.visibility = android.view.View.VISIBLE

                binding.btnDashboard.setOnClickListener {
                    val i = Intent(this, DashboardActivity::class.java)
                    i.putExtra(DashboardActivity.EXTRA_TEACHER_ID, user.id)
                    startActivity(i)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.etSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        initCategoriesAdapter()
        initCoursesAdapter()
        initRegisteredCoursesAdapter()
    }

    private fun initCategoriesAdapter() {
        db.collection(CATEGORIES_COLLECTION).get().addOnSuccessListener { docs ->
            categoriesAdapter.setData(docs.toObjects(Category::class.java))
        }
    }

    private fun initCoursesAdapter() {
        db.collection(COURSES_COLLECTION)
            .limit(20)
            .get()
            .addOnSuccessListener { docs ->
                val courses = docs.toObjects<Course>()
                coursesAdapter.setData(courses)
            }
            .addOnFailureListener {
                Helper.log(this, it.message!!)
                Helper.toast(this, "Error getting registered courses, ${it.message}")
            }
    }

    private fun initRegisteredCoursesAdapter() {
        db.collection(USERS_COLLECTION).document(user!!.uid).get()
            .addOnSuccessListener { doc ->
                if (doc[USER_ACTIVE_COURSES] == null) return@addOnSuccessListener

                val registeredCoursesIds = doc[USER_ACTIVE_COURSES] as ArrayList<*>
                val registeredCourses = ArrayList<Course>()

                registeredCoursesIds.forEach { courseId ->
                    Helper.log(this, courseId.toString())
                    db.collection(COURSES_COLLECTION).document(courseId.toString()).get()
                        .addOnSuccessListener {
                            val img = it[COURSE_IMG] as String
                            val title = it[COURSE_TITLE] as String
                            val cat = it[COURSE_CATEGORY] as String
                            val hours = it[COURSE_HOURS] as Long
                            registeredCourses.add(Course(it.id, img, title, cat, hours))

                            if (courseId.toString() == registeredCoursesIds.last().toString()) {
                                registeredCoursesAdapter.setData(registeredCourses)
                            }
                        }
                        .addOnFailureListener {
                            Helper.log(this, it.message!!)
                            Helper.toast(this, "Error getting registered courses, ${it.message}")
                        }
                }
                registeredCoursesAdapter.setData(registeredCourses)
            }
            .addOnFailureListener {
                Helper.log(this, it.message!!)
                Helper.toast(this, "Error getting registered courses, ${it.message}")
            }
    }

    override fun onClickCourse(course: Course) {
        val intent = Intent(this, CourseDetailsActivity::class.java)
        intent.putExtra(EXTRA_COURSE_ID, course.id)
        intent.putExtra(EXTRA_COURSE_NAME, course.title)
        startActivity(intent)
    }

    override fun onClickRegisteredCourse(course: Course) {
        val intent = Intent(this, CourseDetailsActivity::class.java)
        intent.putExtra(EXTRA_COURSE_ID, course.id)
        intent.putExtra(EXTRA_COURSE_NAME, course.title)
        startActivity(intent)
    }

    override fun onClickCategory(category: Category) {
        val i = Intent(applicationContext, SingleCategoryActivity::class.java)
        i.putExtra(SingleCategoryActivity.CATEGORY_NAME, category.name)
        startActivity(i)
    }
}