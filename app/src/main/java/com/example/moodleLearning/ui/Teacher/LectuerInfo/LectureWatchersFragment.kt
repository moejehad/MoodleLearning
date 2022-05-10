package com.example.moodleLearning.ui.Teacher.LectuerInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moodleLearning.data.adapters.LectureWatchersAdapter
import com.example.moodleLearning.databinding.FragmentLectureWatchersInfoBinding
import com.example.moodleLearning.data.models.*
import com.example.moodleLearning.utils.Constant.COURSES_COLLECTION
import com.example.moodleLearning.utils.Constant.LECTURES_COLLECTION
import com.example.moodleLearning.utils.Constant.LECTURE_WATCHERS_IDS
import com.example.moodleLearning.utils.Constant.USERS_COLLECTION
import com.example.moodleLearning.utils.Helper.Companion.openUrl
import com.example.moodleLearning.utils.Helper.Companion.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class LectureWatchersFragment : BottomSheetDialogFragment() , LectureWatchersAdapter.OnClick {
    private val TAG = this.javaClass.simpleName
    private val db = Firebase.firestore
    private var _binding : FragmentLectureWatchersInfoBinding? = null
    private lateinit var binding : FragmentLectureWatchersInfoBinding
    private lateinit var adapter : LectureWatchersAdapter
    private lateinit var courseId : String
    private lateinit var lectureId : String

    companion object {
        const val EXTRA_COURSE_ID = "courseId"
        const val EXTRA_LECTURE_ID = "lectureId"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLectureWatchersInfoBinding.inflate(inflater, container, false)
        binding = _binding!!

        courseId = requireArguments().getString(EXTRA_COURSE_ID).toString()
        lectureId = requireArguments().getString(EXTRA_LECTURE_ID).toString()

        adapter = LectureWatchersAdapter(requireContext(), this)
        binding.rvAssignments.adapter = adapter

        getWatchersIds()

        return binding.root
    }

    private fun getWatchersIds() {
        db.collection(COURSES_COLLECTION)
            .document(courseId)
            .collection(LECTURES_COLLECTION)
            .document(lectureId)
            .get()
            .addOnSuccessListener { docs ->
                val watchers = docs[LECTURE_WATCHERS_IDS] as ArrayList<String>
                watchers.forEach { doc ->
                    getWatcher(doc)
                }
            }
            .addOnFailureListener {
                toast(requireContext(), "Failed To Get Assignments Information ${it.message}")
                this.dismiss()
            }
    }

    private fun getWatcher(userId : String) {
        db.collection(USERS_COLLECTION).document(userId).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                adapter.setData(User(user!!.id, user.firstName, user.middleName, user.lastName, user.email))
            }
            .addOnFailureListener {
                toast(requireContext(), "Failed To Get Information For Watchers Students  ${it.message}")
                this.dismiss()
            }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onClick(item: User) {
        openUrl(requireContext(), "mailto: ${item.email}")
    }

}