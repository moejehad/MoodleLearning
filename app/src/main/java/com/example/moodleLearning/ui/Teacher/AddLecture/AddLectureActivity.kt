package com.example.moodleLearning.ui.Teacher.AddLecture

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.moodleLearning.databinding.ActivityAddLectureBinding
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.data.models.Lecture
import com.example.moodleLearning.ui.Teacher.LectuerInfo.LectureAssignmentsFragment
import com.example.moodleLearning.ui.Teacher.LectuerInfo.LectureWatchersFragment
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_ID
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_NAME
import com.example.moodleLearning.utils.Constant.EXTRA_LECTURE_ID
import com.example.moodleLearning.utils.Constant.LECTURES_COLLECTION
import com.example.moodleLearning.utils.Constant.LECTURE_DOCS_URL
import com.example.moodleLearning.utils.Constant.LECTURE_VIDEO_URL
import com.example.moodleLearning.utils.FCMService
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_1
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_2
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddLectureActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityAddLectureBinding
    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore
    lateinit var storge: FirebaseStorage
    lateinit var reference: StorageReference
    private var courseId : String? = null
    private var courseName : String? = null
    private var lectureId : String? = null
    private var lectureTitle = ""
    private var lectureUrl = ""
    private var lectureVideo = ""
    lateinit var progressDialog: ProgressDialog
    var path: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLectureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Video")
        progressDialog.setCancelable(false)

        storge = FirebaseStorage.getInstance()

        courseId = intent.getStringExtra(EXTRA_COURSE_ID)
        courseName = intent.getStringExtra(EXTRA_COURSE_NAME)
        lectureId = intent.getStringExtra(EXTRA_LECTURE_ID) // if null, then add new course else update existing course

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        binding.LectureVideo.setOnClickListener {
            pickVideo()
        }

        if (lectureId != null) {
            binding.btnNewLecture.isEnabled = false
            binding.llLectureList.visibility = View.VISIBLE
            binding.screenTitle.text = "Update Lecture"
            binding.btnNewLecture.text = "Update Lecture"
            binding.btnDeleteLecture.visibility = android.view.View.VISIBLE

            db.collection(COURSES_COLLECTION)
                .document(courseId!!)
                .collection(LECTURES_COLLECTION)
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

    val getVideo = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            UploadVideo(it)
        }
    )

    private fun pickVideo() {
        getVideo.launch("Video/*")
    }

    private fun UploadVideo(uri: Uri?) {
        progressDialog.show()
        val fileName = UUID.randomUUID().toString()
        val ref = storge.reference.child("LecturesVideo/$fileName")
        ref.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                path = uri.toString()
            }
            progressDialog.dismiss()
        }.addOnFailureListener { exception ->
            progressDialog.dismiss()
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
            db.collection(COURSES_COLLECTION)
                .document(courseId!!)
                .collection(LECTURES_COLLECTION)
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
                updateLecture(path)
            } else {
                binding.btnNewLecture.text = "Add Lecture"
                binding.screenTitle.text = "Add Lecture"
                addNewLecture(path)
            }
        }
    }

    private fun addNewLecture(imagePath: String) {
        val lecture = Lecture(
            binding.etLectureTitle.text.toString(),
            binding.etLectureUrl.text.toString(),
            imagePath,
            Timestamp.now()
        )

        db.collection(COURSES_COLLECTION)
            .document(courseId!!)
            .collection(LECTURES_COLLECTION)
            .add(lecture)
            .addOnSuccessListener {
                db.collection(COURSES_COLLECTION).document(courseId!!).get().addOnSuccessListener {
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

    private fun updateLecture(imagePath: String) {
        val lectureMap = hashMapOf<String, Any>()
        if (binding.etLectureTitle.text.toString() != lectureTitle) {
            lectureMap[Lecture.LECTURE_TITLE] = binding.etLectureTitle.text.toString()
        }
        if (binding.etLectureUrl.text.toString() != lectureUrl) {
            lectureMap[LECTURE_DOCS_URL] = binding.etLectureUrl.text.toString()
        }
        if (binding.etLectureVideoUrl.text.toString() != lectureVideo) {
            lectureMap[LECTURE_VIDEO_URL] = imagePath
        }

        db.collection(COURSES_COLLECTION)
            .document(courseId!!)
            .collection(LECTURES_COLLECTION)
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