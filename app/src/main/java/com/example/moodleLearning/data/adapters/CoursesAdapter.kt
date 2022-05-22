package com.example.moodleLearning.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.CourseItemBinding
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.utils.Helper.Companion.fillImage

class CoursesAdapter(val context: Context, val click: OnClick) :
    RecyclerView.Adapter<CoursesAdapter.ViewHolder>() {
    private var list = mutableListOf<Course>()

    class ViewHolder(val binding: CourseItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CourseItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = list[position]

        fillImage(context, course.img, holder.binding.ivCourse)
        holder.binding.tvTitle.text = course.title
        holder.binding.tvCategory.text = course.category
        holder.binding.tvHours.text = course.hours.toString() + " Hour"

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
    }

}