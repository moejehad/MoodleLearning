package com.example.moodleLearning.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.LectureStudentsItemBinding
import com.example.moodleLearning.data.models.Assignment
import com.example.moodleLearning.utils.Helper

class LectureAssignmentAdapter(val context: Context) :
    RecyclerView.Adapter<LectureAssignmentAdapter.ViewHolder>() {
    private var list = mutableListOf<Assignment>()

    class ViewHolder(val binding: LectureStudentsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LectureStudentsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.studentName.text = item.userName
        holder.binding.studentEmail.text = item.userEmail
        holder.binding.assignmentDate.text = "${item.date.toDate()}"

        holder.binding.root.setOnClickListener {
            Helper.openUrl(context, item.fileUrl)
        }
    }

    fun setData(data: Assignment) {
        if (list.contains(data)) {
            return
        }

        list.add(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }


}