package com.example.moodleLearning.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moodleLearning.databinding.FragmentIntractorInfoBinding
import com.example.moodleLearning.models.User
import com.example.moodleLearning.utils.Helper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InstructorInfoFragment : BottomSheetDialogFragment() {
    private val TAG = this.javaClass.simpleName
    private val db = Firebase.firestore
    private var _binding : FragmentIntractorInfoBinding? = null
    private lateinit var binding : FragmentIntractorInfoBinding
    private lateinit var instructorId: String

    companion object {
        const val INSTRUCTOR_ID = "InstructorId"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentIntractorInfoBinding.inflate(inflater, container, false)
        binding = _binding!!

        instructorId = requireArguments().getString(INSTRUCTOR_ID).toString()

        db.collection(User.USERS_COLLECTION).document(instructorId).get()
            .addOnSuccessListener { doc ->
                binding.instructorName.text = "${doc[User.USER_FIRST_NAME].toString()} ${doc[User.USER_MIDDLE_NAME].toString()} ${doc[User.USER_FAMILY_NAME].toString()}"
                binding.instructorAddress.text = doc[User.USER_ADDRESS].toString()
                binding.instructorBornDate.text = "Born At " + doc[User.USER_BIRTHDAY].toString()
                binding.instructorBio.text = doc[User.USER_BIO].toString()

                binding.btnCall.setOnClickListener {
                    Helper.openUrl(requireActivity(), "tel: ${doc[User.USER_PHONE].toString()}")
                }
                binding.btnEmail.setOnClickListener {
                    Helper.openUrl(requireActivity(), "mailto: ${doc[User.USER_EMAIL].toString()}")
                }
            }
            .addOnFailureListener {
                Helper.toast(requireContext(), "Failed To Get Instructor Information")
                this.dismiss()
            }

        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}