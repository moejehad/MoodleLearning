package com.example.moodleLearning.ui

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.moodleLearning.databinding.ActivityYtvideoBinding
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.models.Lecture
import com.example.moodleLearning.utils.Helper
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_1
import com.example.moodleLearning.utils.Helper.Companion.YT_SUFFIX_2
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions


class YTVideoActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var binding: ActivityYtvideoBinding
    private val db = Firebase.firestore
    private var videoId: String? = null
    private var lectureId: String? = null
    private var courseId: String? = null

    companion object {
        const val LECTURE_VIDEO = "lectureVideo"
        const val LECTURE_ID = "lectureId"
        const val COURSE_ID = "courseId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityYtvideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        videoId = intent.getStringExtra(LECTURE_VIDEO)!!
        lectureId = intent.getStringExtra(LECTURE_ID)!!
        courseId = intent.getStringExtra(COURSE_ID)!!

        if (videoId!!.startsWith(YT_SUFFIX_1)) {
            videoId = videoId!!.replace(YT_SUFFIX_2, "")
        }
        if (videoId!!.startsWith(YT_SUFFIX_2)) {
            videoId = videoId!!.replace(YT_SUFFIX_2, "")
        }

        toast(this, "We Prepare Video, Please Wait Some Time .")

        val youTubePlayerView = binding.youtubePlayer
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.enterFullScreen();
        youTubePlayerView.enableAutomaticInitialization = false

        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .rel(0)
            .build()

        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId!!, 0f)
                db.collection(Course.COURSES_COLLECTION)
                    .document(courseId!!)
                    .collection(Lecture.LECTURES_COLLECTION)
                    .document(lectureId!!)
                    .update(Lecture.LECTURE_WATCHERS_IDS, FieldValue.arrayUnion(Helper.getCurrentUser()!!.uid))
            }
        }, true, iFramePlayerOptions)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayer.release()
    }
}