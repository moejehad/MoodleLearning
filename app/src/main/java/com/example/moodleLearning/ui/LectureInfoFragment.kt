package com.example.moodleLearning.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moodleLearning.databinding.FragmentLectureInfoBinding
import com.example.moodleLearning.models.Assignment
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.models.Lecture
import com.example.moodleLearning.utils.Helper
import com.example.moodleLearning.utils.Helper.Companion.openUrl
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class LectureInfoFragment : BottomSheetDialogFragment() {
    private val TAG = this.javaClass.simpleName
    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private var _binding : FragmentLectureInfoBinding? = null
    private lateinit var binding : FragmentLectureInfoBinding
    private var lectureId : String? = null

    companion object {
        const val LECTURE_VIDEO = "lectureVideo"
        const val LECTURE_DOCS = "lectureDocs"
        const val LECTURE_ID = "lectureId"
        const val COURSE_ID = "courseId"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLectureInfoBinding.inflate(inflater, container, false)
        binding = _binding!!

        lectureId = arguments?.getString(LECTURE_ID)
        val lectureVideoUrl = arguments?.getString(LECTURE_VIDEO)
        val lectureDocsUrl = arguments?.getString(LECTURE_DOCS)
        val courseId = arguments?.getString(COURSE_ID)

        binding.btnWatch.setOnClickListener {
            val i = Intent(activity, YTVideoActivity::class.java)
            i.putExtra(LECTURE_VIDEO, lectureVideoUrl)
            i.putExtra(LECTURE_ID, lectureId)
            i.putExtra(COURSE_ID, courseId)
            activity?.startActivity(i)
        }

        binding.btnSeeDocs.setOnClickListener {
            if (lectureDocsUrl != null && lectureDocsUrl.isNotEmpty()) {
                openUrl(requireActivity(), lectureDocsUrl)
            } else {
                toast(requireActivity(), "No documents available")
            }
        }

        binding.btnSubmitAssignment.setOnClickListener {
            if (binding.etLectureAssignment.text.isNotEmpty() && Patterns.WEB_URL.matcher(binding.etLectureAssignment.text.toString()).matches()) {
                db.collection(Course.COURSES_COLLECTION)
                    .document(courseId!!)
                    .collection(Lecture.LECTURES_COLLECTION)
                    .document(lectureId!!)
                    .collection(Assignment.COLLECTION_NAME)
                    .add(Assignment("", binding.etLectureAssignment.text.toString(), Helper.getCurrentUser()!!.uid, Timestamp.now()))
                    .addOnSuccessListener {
                        toast(requireActivity(), "Assignment submitted successfully")
                        this.dismiss()
                    }
                    .addOnFailureListener {
                        toast(requireActivity(), "Assignment submission failed, ${it.message}")
                    }
            } else {
                toast(requireActivity(), "Please enter your assignment link, should be a valid URL")
            }
        }

        return binding.root
    }
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}