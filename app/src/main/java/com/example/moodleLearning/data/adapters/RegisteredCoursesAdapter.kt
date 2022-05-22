package com.example.moodleLearning.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.RegisteredCoursesItemBinding
import com.example.moodleLearning.data.models.Course
import com.example.moodleLearning.utils.Helper

class RegisteredCoursesAdapter(val context: Context, val click: OnClick) :
    RecyclerView.Adapter<RegisteredCoursesAdapter.ViewHolder>() {
    private var list = mutableListOf<Course>()

    class ViewHolder(val binding: RegisteredCoursesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RegisteredCoursesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = list[position]

        Helper.fillImage(context, course.img, holder.binding.ivCourse)
        holder.binding.tvCourseName.text = course.title + " (" + course.category + ")"

        holder.binding.root.setOnClickListener {
            click.onClickRegisteredCourse(course)
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
        fun onClickRegisteredCourse(course: Course)
    }

}