package com.example.moodleLearning.ui.Teacher.AddCourse

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.moodleLearning.R
import com.example.moodleLearning.databinding.ActivityAddCourseBinding
import com.example.moodleLearning.data.models.Category
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.utils.Constant.CATEGORIES_COLLECTION
import com.example.moodleLearning.utils.Constant.CATEGORIES_NAME
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_CATEGORY
import com.example.moodleLearning.utils.Constant.COURSE_DESC
import com.example.moodleLearning.utils.Constant.COURSE_HOURS
import com.example.moodleLearning.utils.Constant.COURSE_IMG
import com.example.moodleLearning.utils.Constant.COURSE_LAST_UPDATE_DATE
import com.example.moodleLearning.utils.Constant.COURSE_TITLE
import com.example.moodleLearning.utils.Helper.Companion.fillImage
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*


class AddCourseActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityAddCourseBinding
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private var teacherId = ""
    private var courseId : String? = null
    private var courseTitle : String? = null
    private var courseDescription : String? = null
    private var courseHours : Long? = null
    private var courseCategory : String? = null
    private lateinit var pd: ProgressDialog
    private var courseImg : String? = null
    private var imageUri : Uri? = null

    companion object {
        const val EXTRA_TEACHER_ID = "teacherId"
        const val EXTRA_COURSE_ID = "courseId"
        const val EXTRA_COURSE_NAME = "courseTitle"
        private const val SELECT_PICTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pd = ProgressDialog(this)

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        teacherId = intent.getStringExtra(EXTRA_TEACHER_ID).toString()
        courseId = intent.getStringExtra(EXTRA_COURSE_ID) // if null, then add new course else update existing course
        courseTitle = intent.getStringExtra(EXTRA_COURSE_NAME) // if null, then add new course else update existing course

        val categoriesList = arrayListOf<String>()
        db.collection(CATEGORIES_COLLECTION).get().addOnSuccessListener { docs ->
            docs.forEach {
                categoriesList.add(it.data[CATEGORIES_NAME].toString())
            }
            val adapter = ArrayAdapter(this, R.layout.list_item, categoriesList)
            (binding.textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        }

        if (courseId != null) {
            binding.btnAddCourse.isEnabled = false
            binding.btnAddCourse.text = "Update Course"
            binding.screenTitle.text = "Update $courseTitle"
            db.collection(COURSES_COLLECTION).document(courseId!!).get()
                .addOnSuccessListener { document ->
                    val course = document.toObject(Course::class.java)!!

                    courseTitle = course.title
                    courseDescription = course.desc
                    courseHours = course.hours
                    courseCategory = course.category
                    courseImg = course.img

                    fillImage(this, courseImg!!, binding.ivCourse)
                    binding.etCourseTitle.setText(course.title)
                    binding.etCourseDescription.setText(course.desc)
                    binding.etCourseHours.setText(course.hours.toString())
                    binding.etCategory.setText(course.category)

                    binding.btnAddCourse.isEnabled = true
                }
                .addOnFailureListener {
                    toast(applicationContext, "Failed to get course data")
                    finish()
                }
        }

        val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    imageUri = fileUri
                    binding.ivCourse.setImageURI(fileUri)
                }
                ImagePicker.RESULT_ERROR -> {
                    toast(applicationContext, "Error: ${ImagePicker.getError(data)}")
                }
                else -> {
                    toast(applicationContext, "Task Canceled")
                }
            }
        }

        binding.ivCourse.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.btnAddCourse.setOnClickListener {
            if (courseId != null) {
                pd.setTitle("Updating course")
                pd.show()
                checkUpdateCourse()
            } else {
                pd.setTitle("Add New ${binding.etCourseTitle.text}")
                pd.show()
                checkNewCourseInfo()
            }
        }
    }

    private fun checkNewCourseInfo() {
        if (
            imageUri != null &&
            binding.etCourseTitle.text.toString().isNotEmpty() &&
            binding.etCourseDescription.text.toString().isNotEmpty() &&
            binding.etCourseHours.text.toString().isNotEmpty() &&
            binding.etCategory.text.toString().isNotEmpty()) {

            uploadCourseImage(imageUri!!)
        } else {
            toast(applicationContext, "Please fill all the fields")
            pd.dismiss()
        }
    }

    private fun checkUpdateCourse() {
        if (
            binding.etCourseTitle.text.toString().isNotEmpty() &&
            binding.etCourseDescription.text.toString().isNotEmpty() &&
            binding.etCourseHours.text.toString().isNotEmpty() &&
            binding.etCategory.text.toString().isNotEmpty()) {
                if (imageUri != null) {
                    uploadCourseImage(imageUri!!, true)
                } else {
                    updateCourse(null)
                }
        } else {
            toast(applicationContext, "Please fill all the fields")
            pd.dismiss()
        }
    }

    private fun uploadCourseImage(imageUri: Uri, isUpdate: Boolean = false) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("CoursesImages/$fileName")
        ref.putFile(imageUri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                courseImg = it.toString()
                if (isUpdate) {
                    updateCourse(courseImg!!)
                } else {
                    val course = Course(courseImg!!, binding.etCourseTitle.text.toString(), binding.etCategory.text.toString(), binding.etCourseDescription.text.toString(), teacherId, binding.etCourseHours.text.toString().toLong())
                    saveNewCourse(course)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                toast(applicationContext, "Failed to get image url ${it.message}, try again")
                pd.dismiss()
            }
        }.addOnFailureListener {
            it.printStackTrace()
            toast(applicationContext, "Failed to upload image ${it.message}, try again")
            pd.dismiss()
        }
    }

    private fun saveNewCourse(course: Course) {
        db.collection(COURSES_COLLECTION)
            .add(course)
            .addOnSuccessListener {
                toast(applicationContext, "Course added successfully")
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                toast(applicationContext, "Failed to add course ${it.message}")
                pd.dismiss()
            }
    }

    private fun updateCourse(courseImg: String?) {
        val courseMap = hashMapOf<String, Any>()

        if (courseImg != null) {
            courseMap[COURSE_IMG] = courseImg
        }
        if (binding.etCourseTitle.text.toString() != courseTitle) {
            courseMap[COURSE_TITLE] = binding.etCourseTitle.text.toString()
        }
        if (binding.etCourseDescription.text.toString() != courseDescription) {
            courseMap[COURSE_DESC] = binding.etCourseDescription.text.toString()
        }
        if (binding.etCourseHours.text.toString() != courseHours.toString()) {
            courseMap[COURSE_HOURS] = binding.etCourseHours.text.toString().toLong()
        }
        if (binding.etCategory.text.toString() != courseCategory) {
            courseMap[COURSE_CATEGORY] = binding.etCategory.text.toString()
        }
        courseMap[COURSE_LAST_UPDATE_DATE] = Timestamp.now()


        db.collection(COURSES_COLLECTION).document(courseId!!)
            .update(courseMap)
            .addOnSuccessListener {
                toast(applicationContext, "Course Updated successfully")
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                toast(applicationContext, "Failed to Update course ${it.message}")
            }
    }
}