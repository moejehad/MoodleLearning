package com.example.moodleLearning.ui.LectureDetails

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import com.example.moodleLearning.R
import com.example.moodleLearning.data.models.Assignment
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.COURSE_ID
import com.example.moodleLearning.utils.Constant.LECTURES_COLLECTION
import com.example.moodleLearning.utils.Constant.LECTURE_DOCS
import com.example.moodleLearning.utils.Constant.LECTURE_ID
import com.example.moodleLearning.utils.Constant.LECTURE_TITLE
import com.example.moodleLearning.utils.Constant.LECTURE_VIDEO
import com.example.moodleLearning.utils.Constant.LECTURE_WATCHERS
import com.example.moodleLearning.utils.Constant.LECTURE_WATCHERS_IDS
import com.example.moodleLearning.utils.Helper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_lecture_details.*

class LectureDetailsActivity : AppCompatActivity() {

    private var db = Firebase.firestore
    lateinit var exoPlayer: PlayerView
    private lateinit var simpleExoPlayerView: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var urlType: UrlType

    private var lectureTitle: String = ""
    private var lectureVideo: String = ""
    private var lectureWatchers: String? = null
    private var lectureDocs: String = ""
    private var lectureId: String = ""
    private var courseID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_details)

        lectureTitle = intent.getStringExtra(LECTURE_TITLE)!!
        lectureVideo = intent.getStringExtra(LECTURE_VIDEO)!!
        lectureWatchers = intent.getStringExtra(LECTURE_WATCHERS)
        lectureDocs = intent.getStringExtra(LECTURE_DOCS)!!
        lectureId = intent.getStringExtra(LECTURE_ID)!!
        courseID = intent.getStringExtra(COURSE_ID)!!

        updateWatchers(courseID!!)
        screenTitle.text = lectureTitle
        lectureTitleText.text = lectureTitle
        lectureWatchersText.text = lectureWatchers + " View"

        submitAssigmentsToFirestore()
        seeDocs()

        exoPlayer = findViewById(R.id.exoplayer)
        initPlayer(lectureVideo!!)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        backIcon.setOnClickListener {
            onBackPressed()
        }

    }

    private fun seeDocs() {
        btnSeeDocs.setOnClickListener {
            if (lectureDocs != null && lectureDocs.isNotEmpty()) {
                Helper.openUrl(this, lectureDocs)
            } else {
                Helper.toast(this, "No documents available")
            }
        }
    }

    private fun submitAssigmentsToFirestore() {
        btnSubmitAssignment.setOnClickListener {
            if (etLectureAssignment.text.isNotEmpty() && Patterns.WEB_URL.matcher(
                    etLectureAssignment.text.toString()
                ).matches()
            ) {
                db.collection(COURSES_COLLECTION)
                    .document(courseID!!)
                    .collection(LECTURES_COLLECTION)
                    .document(lectureId!!)
                    .collection(Assignment.COLLECTION_NAME)
                    .add(
                        Assignment(
                            "",
                            etLectureAssignment.text.toString(),
                            Helper.getCurrentUser()!!.uid,
                            Timestamp.now()
                        )
                    )
                    .addOnSuccessListener {
                        etLectureAssignment.visibility = View.GONE
                        btnSubmitAssignment.visibility = View.GONE
                        Helper.toast(this, "Assignment submitted successfully")
                    }
                    .addOnFailureListener {
                        Helper.toast(
                            this,
                            "Assignment submission failed, ${it.message}"
                        )
                    }
            } else {
                Helper.toast(
                    this,
                    "Please enter your assignment link, should be a valid URL"
                )
            }
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

    private fun updateWatchers(courseID: String) {
        db.collection(COURSES_COLLECTION)
            .document(courseID!!)
            .collection(LECTURES_COLLECTION)
            .document(lectureId!!)
            .update(LECTURE_WATCHERS_IDS, FieldValue.arrayUnion(Helper.getCurrentUser()!!.uid))
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayerView.playWhenReady = true
        simpleExoPlayerView.play()
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
