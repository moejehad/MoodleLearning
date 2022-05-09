package com.example.moodleLearning.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.TeacherCourseItemBinding
import com.example.moodleLearning.models.Course
import com.example.moodleLearning.utils.Helper

class TeacherCoursesAdapter(val context: Context, val click: OnClick) : RecyclerView.Adapter<TeacherCoursesAdapter.ViewHolder>() {
    private var list = mutableListOf<Course>()

    // For View Binding
    class ViewHolder(val binding: TeacherCourseItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TeacherCourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = list[position]

        Helper.fillImage(context, course.img, holder.binding.ivCourse)
        holder.binding.tvCourseName.text = course.title + " (" + course.category + ")"

        holder.binding.btnDeleteCourse.setOnClickListener {
            click.onClickDeleteCourse(course)
        }
        holder.binding.btnEditCourse.setOnClickListener {
            click.onClickEditCourse(course)
        }
        holder.binding.root.setOnClickListener {
            click.onClickCourse(course)
        }
    }

    fun setData(data: List<Course>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }

    interface OnClick {
        fun onClickCourse(course: Course)
        fun onClickDeleteCourse(course: Course)
        fun onClickEditCourse(course: Course)
    }

}