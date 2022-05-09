package com.example.moodleLearning.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.R
import com.example.moodleLearning.databinding.LectureItemBinding
import com.example.moodleLearning.models.Lecture
import com.example.moodleLearning.utils.Helper
import com.example.moodleLearning.utils.Helper.Companion.formatDate

class LecturesAdapter(val context: Context, val click: OnClick, val isTeacher: Boolean) : RecyclerView.Adapter<LecturesAdapter.ViewHolder>() {
    private var list = mutableListOf<Lecture>()

    // For View Binding
    class ViewHolder(val binding: LectureItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LectureItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lecture = list[position]

        holder.binding.tvTitle.text = lecture.title
        holder.binding.tvDate.text = formatDate(lecture.date)
        holder.binding.tvWatchNumber.text = lecture.watchersIds.size.toString() + " view"

        if (lecture.watchersIds.contains(Helper.getCurrentUser()?.uid)) {
            holder.binding.ivLecture.setImageResource(R.drawable.ic_check)
            holder.binding.lectureItemLayout.setBackgroundColor(Color.LTGRAY)
        }

        holder.binding.root.setOnClickListener {
            if (isTeacher) click.onTeacherClickLecture(lecture)
            else click.onStudentClickLecture(lecture, holder.layoutPosition)
        }
    }

    fun setData(data: List<Lecture>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }

    interface OnClick {
        fun onStudentClickLecture(lecture: Lecture, position: Int) {}
        fun onTeacherClickLecture(lecture: Lecture) {}
    }
}