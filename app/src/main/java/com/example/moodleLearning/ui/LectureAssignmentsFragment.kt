package com.example.moodleLearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moodleLearning.adapters.LectureAssignmentAdapter
import com.example.moodleLearning.databinding.FragmentLectureStudentsInfoBinding
import com.example.moodleLearning.models.*
import com.example.moodleLearning.utils.Helper.Companion.openUrl
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LectureAssignmentsFragment : BottomSheetDialogFragment() , LectureAssignmentAdapter.OnClick {
    private val TAG = this.javaClass.simpleName
    private val db = Firebase.firestore
    private var _binding : FragmentLectureStudentsInfoBinding? = null
    private lateinit var binding : FragmentLectureStudentsInfoBinding
    private lateinit var adapter : LectureAssignmentAdapter
    private lateinit var courseId : String
    private lateinit var lectureId : String

    companion object {
        const val EXTRA_COURSE_ID = "courseId"
        const val EXTRA_LECTURE_ID = "lectureId"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLectureStudentsInfoBinding.inflate(inflater, container, false)
        binding = _binding!!

        courseId = requireArguments().getString(EXTRA_COURSE_ID).toString()
        lectureId = requireArguments().getString(EXTRA_LECTURE_ID).toString()

        adapter = LectureAssignmentAdapter(requireContext(), this)
        binding.rvAssignments.adapter = adapter

        getAssignments()

        return binding.root
    }

    private fun getAssignments() {
        db.collection(Course.COURSES_COLLECTION)
            .document(courseId)
            .collection(Lecture.LECTURES_COLLECTION)
            .document(lectureId)
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
                toast(requireContext(), "Failed To Get Assignments Information ${it.message}")
                this.dismiss()
            }
    }

    private fun getAssignmentUser(userId : String, id: String, url: String, date: Timestamp) {
        db.collection(User.USERS_COLLECTION).document(userId).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                adapter.setData(Assignment(id, url, date, user?.firstName + " " + user?.middleName + " " + user?.lastName, user?.email ?: "No Email"))
            }
            .addOnFailureListener {
                toast(requireContext(), "Failed To Get Assignment Student Information ${it.message}")
                this.dismiss()
            }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onClick(assignment: Assignment) {
        openUrl(requireContext(), assignment.fileUrl)
    }
}