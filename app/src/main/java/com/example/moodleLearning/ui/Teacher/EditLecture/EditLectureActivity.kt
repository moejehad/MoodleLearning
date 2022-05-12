package com.example.moodleLearning.ui.Teacher.EditLecture

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.moodleLearning.R
import com.example.moodleLearning.data.adapters.LectureAssignmentAdapter
import com.example.moodleLearning.data.adapters.LectureWatchersAdapter
import com.example.moodleLearning.data.models.Assignment
import com.example.moodleLearning.data.models.User
import com.example.moodleLearning.utils.Constant
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_ID
import com.example.moodleLearning.utils.Constant.COURSE_TITLE
import com.example.moodleLearning.utils.Constant.EXTRA_COURSE_NAME
import com.example.moodleLearning.utils.Constant.LECTURES_COLLECTION
import com.example.moodleLearning.utils.Constant.LECTURE_DOCS
import com.example.moodleLearning.utils.Constant.LECTURE_DOCS_URL
import com.example.moodleLearning.utils.Constant.LECTURE_ID
import com.example.moodleLearning.utils.Constant.LECTURE_TITLE
import com.example.moodleLearning.utils.Constant.LECTURE_VIDEO
import com.example.moodleLearning.utils.Constant.LECTURE_VIDEO_URL
import com.example.moodleLearning.utils.Helper
import com.example.moodleLearning.utils.Helper.Companion.openUrl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_lecture.*
import java.util.*


class EditLectureActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    lateinit var storge: FirebaseStorage

    private lateinit var adapter : LectureWatchersAdapter
    private lateinit var AssignmentAdapter : LectureAssignmentAdapter

    lateinit var exoPlayer: PlayerView
    private lateinit var simpleExoPlayerView: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var urlType: UrlType

    private var courseId: String? = null
    private var courseName: String? = null
    private var lectureId: String? = null
    private var lectureTitle = ""
    private var lectureUrlDocs = ""
    private var lectureVideo = ""

    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_lecture)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Video")
        progressDialog.setCancelable(false)

        storge = FirebaseStorage.getInstance()

        courseId = intent.getStringExtra(COURSE_ID)
        courseName = intent.getStringExtra(EXTRA_COURSE_NAME)
        lectureId = intent.getStringExtra(LECTURE_ID)
        lectureTitle = intent.getStringExtra(LECTURE_TITLE)!!
        lectureVideo = intent.getStringExtra(LECTURE_VIDEO)!!
        lectureUrlDocs = intent.getStringExtra(LECTURE_DOCS)!!

        etLectureTitle.setText(lectureTitle)
        etLectureUrl.setText(lectureUrlDocs)

        exoPlayer = findViewById(R.id.EditLectureExoplayer)
        initPlayer(lectureVideo!!)

        adapter = LectureWatchersAdapter(this)
        rvWatchers.adapter = adapter
        getWatchersIds()

        AssignmentAdapter = LectureAssignmentAdapter(this)
        rvAssignments.adapter = adapter
        getAssignments()

        deleteAssigments()

        LectureVideo.setOnClickListener {
            pickVideo()
        }

        btnNewLecture.setOnClickListener {
            updateLecture(lectureVideo)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onResume() {
        super.onResume()

        simpleExoPlayerView.playWhenReady = true
        simpleExoPlayerView.play()

    }

    private fun updateLecture(imagePath: String) {
        val lectureMap = hashMapOf<String, Any>()
        if (etLectureTitle.text.toString() != lectureTitle) {
            lectureMap[COURSE_TITLE] = etLectureTitle.text.toString()
        }
        if (etLectureUrl.text.toString() != lectureUrlDocs) {
            lectureMap[LECTURE_DOCS_URL] = etLectureUrl.text.toString()
        }

        lectureMap[LECTURE_VIDEO_URL] = imagePath

        db.collection(COURSES_COLLECTION)
            .document(courseId!!)
            .collection(LECTURES_COLLECTION)
            .document(lectureId!!)
            .update(lectureMap)
            .addOnSuccessListener {
                Helper.toast(applicationContext, "Lecture Updated Successfully")
                finish()
            }
            .addOnFailureListener {
                Helper.toast(applicationContext, "Failed to Updated lecture ${it.message}")
            }
    }

    private fun deleteAssigments() {
        btnDeleteLecture.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Delete Lecture")
                setMessage("Are you sure you want to delete this Lecture?")
                setPositiveButton("Delete Lecture") { _, _ ->
                    db.collection(Constant.COURSES_COLLECTION)
                        .document(courseId!!)
                        .collection(Constant.LECTURES_COLLECTION)
                        .document(lectureId!!)
                        .delete()
                        .addOnSuccessListener {
                            Helper.toast(applicationContext, "Lecture deleted")
                            finish()
                        }
                        .addOnFailureListener {
                            Helper.toast(
                                applicationContext,
                                "Failed to delete lecture ${it.message}"
                            )
                        }
                }
                setNegativeButton("Cancel") { _, _ -> }
                show()
            }
        }
    }


    private fun getWatchersIds() {
        db.collection(COURSES_COLLECTION)
            .document(courseId.toString())
            .collection(LECTURES_COLLECTION)
            .document(lectureId.toString())
            .get()
            .addOnSuccessListener { docs ->
                val watchers = docs[Constant.LECTURE_WATCHERS_IDS] as ArrayList<String>
                watchers.forEach { doc ->
                    getWatcher(doc)
                }
            }
            .addOnFailureListener {
                Helper.toast(
                    this,
                    "Failed To Get Watchers Information ${it.message}"
                )
            }
    }

    private fun getWatcher(userId : String) {
        db.collection(Constant.USERS_COLLECTION).document(userId).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                adapter.setData(User(user!!.id, user.firstName, user.middleName, user.lastName, user.email))
            }
            .addOnFailureListener {
                Helper.toast(
                    this,
                    "Failed To Get Information For Watchers Students  ${it.message}"
                )
            }
    }


    private fun getAssignments() {
        db.collection(COURSES_COLLECTION)
            .document(courseId.toString())
            .collection(LECTURES_COLLECTION)
            .document(lectureId.toString())
            .collection(Assignment.COLLECTION_NAME)
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    docs.toObjects(Assignment::class.java).forEach {
                        getAssignmentUser(it.userId, it.id, it.fileUrl, it.date)
                    }
                }
            }
            .addOnFailureListener {
                Helper.toast(
                    this,
                    "Failed To Get Assignments Information ${it.message}"
                )
        }
    }

    private fun getAssignmentUser(userId : String, id: String, url: String, date: Timestamp) {
        db.collection(Constant.USERS_COLLECTION).document(userId).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                AssignmentAdapter.setData(Assignment(id, url, date, user?.firstName + " " + user?.middleName + " " + user?.lastName, user?.email ?: "No Email"))
            }
            .addOnFailureListener {
                Helper.toast(
                    this,
                    "Failed To Get Assignment Student Information ${it.message}"
                )
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
                lectureVideo = uri.toString()
                uploadSuccess.visibility = View.VISIBLE
            }
            progressDialog.dismiss()
        }.addOnFailureListener { exception ->
            progressDialog.dismiss()
        }
    }

    private fun initPlayer(lecture: String) {
        simpleExoPlayerView = SimpleExoPlayer.Builder(this).build()
        exoPlayer.player = simpleExoPlayerView
        createMediaSource(lecture!!)
        simpleExoPlayerView.setMediaSource(mediaSource)
        simpleExoPlayerView.prepare()
    }

    private fun createMediaSource(url: String) {

        urlType = UrlType.MP4
        urlType.url = url

        simpleExoPlayerView.seekTo(0)
        when (urlType) {
            UrlType.MP4 -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    this,
                    Util.getUserAgent(this, applicationInfo.name)
                )
                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
            UrlType.HLS -> {
                val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                    this,
                    Util.getUserAgent(this, applicationInfo.name)
                )
                mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
        }
    }

    private var playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            if (urlType == UrlType.HLS) {
                exoPlayer.useController = false
            }
            if (urlType == UrlType.MP4) {
                exoPlayer.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)

        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayerView.pause()
        simpleExoPlayerView.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayerView.pause()
        simpleExoPlayerView.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()

        simpleExoPlayerView.removeListener(playerListener)
        simpleExoPlayerView.pause()
        simpleExoPlayerView.clearMediaItems()

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

}

enum class UrlType(var url: String) {
    MP4(""), HLS("")
}
