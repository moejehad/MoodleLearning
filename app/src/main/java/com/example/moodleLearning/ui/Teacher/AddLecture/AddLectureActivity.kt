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
import com.example.moodleLearning.data.models.Lecture
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_ID
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_NAME
import com.example.moodleLearning.utils.Constant.EXTRA_LECTURE_ID
import com.example.moodleLearning.utils.Constant.LECTURES_COLLECTION
import com.example.moodleLearning.utils.FCMService
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
    private var courseId: String? = null
    private var courseName: String? = null
    private var lectureId: String? = null
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
        lectureId = intent.getStringExtra(EXTRA_LECTURE_ID)

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        binding.LectureVideo.setOnClickListener {
            pickVideo()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.btnNewLecture.setOnClickListener {
            if (binding.etLectureTitle.text.toString().isEmpty()) {
                toast(applicationContext, "Please enter lecture title")
                return@setOnClickListener
            }
            if (!URLUtil.isValidUrl(binding.etLectureUrl.text.toString())) {
                toast(applicationContext, "Please enter valid lecture url")
                return@setOnClickListener
            }

            binding.btnNewLecture.text = "Add Lecture"
            binding.screenTitle.text = "Add Lecture"
            addNewLecture(path)

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
                    FCMService.sendRemoteNotification(
                        "New lecture added to $courseName",
                        "${lecture.title} added to $courseName",
                        null,
                        courseId!!
                    )
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

    val getVideo = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            UploadVideo(it)
        }
    )

    private fun pickVideo() {
        getVideo.launch("video/*")
    }

    private fun UploadVideo(uri: Uri?) {
        progressDialog.show()
        val fileName = UUID.randomUUID().toString()
        val ref = storge.reference.child("LecturesVideo/$fileName")
        ref.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                path = uri.toString()
                binding.uploadSuccess.visibility = View.VISIBLE
            }
            progressDialog.dismiss()
        }.addOnFailureListener { exception ->
            progressDialog.dismiss()
        }

    }


}