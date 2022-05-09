package com.example.moodleLearning.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import com.example.moodleLearning.databinding.ActivityAddLectureBinding
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.models.Lecture
import com.example.moodleLearning.utils.FCMService
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_1
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_2
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddLectureActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityAddLectureBinding
    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private var courseId : String? = null
    private var courseName : String? = null
    private var lectureId : String? = null
    private var lectureTitle = ""
    private var lectureUrl = ""
    private var lectureVideo = ""

    companion object {
        const val EXTRA_COURSE_ID = "courseId"
        const val EXTRA_COURSE_NAME = "courseName"
        const val EXTRA_LECTURE_ID = "lectureId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLectureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra(EXTRA_COURSE_ID)
        courseName = intent.getStringExtra(EXTRA_COURSE_NAME)
        lectureId = intent.getStringExtra(EXTRA_LECTURE_ID) // if null, then add new course else update existing course

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        if (lectureId != null) {
            binding.btnNewLecture.isEnabled = false
            binding.llLectureList.visibility = View.VISIBLE
            binding.screenTitle.text = "Update Lecture"
            binding.btnNewLecture.text = "Update Lecture"
            binding.btnDeleteLecture.visibility = android.view.View.VISIBLE

            db.collection(Course.COURSES_COLLECTION)
                .document(courseId!!)
                .collection(Lecture.LECTURES_COLLECTION)
                .document(lectureId!!)
                .get()
                .addOnSuccessListener {
                    val lecture = it.toObject(Lecture::class.java)

                    binding.etLectureTitle.setText(lecture?.title)
                    binding.etLectureUrl.setText(lecture?.docsUrl)
                    binding.etLectureVideoUrl.setText(lecture?.videoUrl)

                    lectureTitle = lecture?.title.toString()
                    lectureUrl = lecture?.docsUrl.toString()
                    lectureVideo = lecture?.videoUrl.toString()
                }

            binding.btnNewLecture.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()

        binding.btnAssignments.setOnClickListener {
            LectureAssignmentsFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(LectureAssignmentsFragment.EXTRA_LECTURE_ID, lectureId)
                        putString(LectureAssignmentsFragment.EXTRA_COURSE_ID, courseId)
                    }
                }
                .show(supportFragmentManager, "LectureInfoFragment")
        }

        binding.btnWatchers.setOnClickListener {
            LectureWatchersFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(LectureWatchersFragment.EXTRA_LECTURE_ID, lectureId)
                        putString(LectureWatchersFragment.EXTRA_COURSE_ID, courseId)
                    }
                }
                .show(supportFragmentManager, "LectureInfoFragment")
        }

        binding.btnDeleteLecture.setOnClickListener {
            db.collection(Course.COURSES_COLLECTION)
                .document(courseId!!)
                .collection(Lecture.LECTURES_COLLECTION)
                .document(lectureId!!)
                .delete()
                .addOnSuccessListener {
                    toast(applicationContext, "Lecture deleted")
                    finish()
                }
                .addOnFailureListener {
                    toast(applicationContext, "Failed to delete lecture ${it.message}")
                }
        }

        binding.btnNewLecture.setOnClickListener {
            if (binding.etLectureTitle.text.toString().isEmpty()) {
                toast(applicationContext, "Please enter lecture title")
                return@setOnClickListener
            }
            if (!URLUtil.isValidUrl(binding.etLectureUrl.text.toString())) {
                toast(applicationContext, "Please enter valid lecture url")
                return@setOnClickListener
            }
            if (!binding.etLectureVideoUrl.text.startsWith(YT_SUFFIX_1) && !binding.etLectureVideoUrl.text.startsWith(YT_SUFFIX_2)) {
                toast(applicationContext, "Please enter lecture Youtube video url")
                return@setOnClickListener
            }

            if (lectureId != null) {
                binding.btnNewLecture.text = "Update Lecture"
                binding.screenTitle.text = "Update Lecture"
                updateLecture()
            } else {
                binding.btnNewLecture.text = "Add Lecture"
                binding.screenTitle.text = "Add Lecture"
                addNewLecture()
            }
        }
    }

    private fun addNewLecture() {
        val lecture = Lecture(
            binding.etLectureTitle.text.toString(),
            binding.etLectureUrl.text.toString(),
            binding.etLectureVideoUrl.text.toString(),
            Timestamp.now()
        )

        db.collection(Course.COURSES_COLLECTION)
            .document(courseId!!)
            .collection(Lecture.LECTURES_COLLECTION)
            .add(lecture)
            .addOnSuccessListener {
                db.collection(Course.COURSES_COLLECTION).document(courseId!!).get().addOnSuccessListener {
                    val emails = it[Course.COURSE_REGISTERS_EMAILS] as List<String>
//                    JavaMailAPI(emails, "New Lecture Add To $courseName", "New Lecture (${binding.etLectureTitle.text}) Added To Course $courseName").execute()
                    FCMService.sendRemoteNotification("New lecture added to $courseName", "${lecture.title} added to $courseName", null, courseId!!)
                    toast(applicationContext, "Lecture added successfully")
                    finish()
                }.addOnFailureListener {
                    toast(applicationContext, "Failed To Add Course")
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                toast(applicationContext, "Failed to add lecture ${it.message}")
            }
    }

    private fun updateLecture() {
        val lectureMap = hashMapOf<String, Any>()
        if (binding.etLectureTitle.text.toString() != lectureTitle) {
            lectureMap[Lecture.LECTURE_TITLE] = binding.etLectureTitle.text.toString()
        }
        if (binding.etLectureUrl.text.toString() != lectureUrl) {
            lectureMap[Lecture.LECTURE_DOCS_URL] = binding.etLectureUrl.text.toString()
        }
        if (binding.etLectureVideoUrl.text.toString() != lectureVideo) {
            lectureMap[Lecture.LECTURE_VIDEO_URL] = binding.etLectureVideoUrl.text.toString()
        }

        db.collection(Course.COURSES_COLLECTION)
            .document(courseId!!)
            .collection(Lecture.LECTURES_COLLECTION)
            .document(lectureId!!)
            .update(lectureMap)
            .addOnSuccessListener {
                toast(applicationContext, "Lecture added successfully")
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                toast(applicationContext, "Failed to add lecture ${it.message}")
            }
    }
}