package com.example.moodleLearning.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.LectureStudentsItemBinding
import com.example.moodleLearning.models.Assignment

class LectureAssignmentAdapter(val context: Context, val click: OnClick) : RecyclerView.Adapter<LectureAssignmentAdapter.ViewHolder>() {
    private var list = mutableListOf<Assignment>()

    // For View Binding
    class ViewHolder(val binding: LectureStudentsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LectureStudentsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.studentName.text = item.userName
        holder.binding.studentEmail.text = item.userEmail
        holder.binding.assignmentDate.text = "${item.date.toDate()}"

        holder.binding.root.setOnClickListener {
            click.onClick(item)
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

    interface OnClick {
        fun onClick(item: Assignment)
    }

}