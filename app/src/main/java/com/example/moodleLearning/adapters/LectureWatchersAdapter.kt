package com.example.moodleLearning.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.LectureWatchersItemBinding
import com.example.moodleLearning.models.User

class LectureWatchersAdapter(val context: Context, val click: OnClick) : RecyclerView.Adapter<LectureWatchersAdapter.ViewHolder>() {
    private var list = mutableListOf<User>()

    // For View Binding
    class ViewHolder(val binding: LectureWatchersItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LectureWatchersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.studentName.text = item.firstName + " " + item.middleName+ " " + item.lastName
        holder.binding.studentEmail.text = item.email

        holder.binding.root.setOnClickListener {
            click.onClick(item)
        }
    }

    fun setData(data: User) {
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
        fun onClick(item: User)
    }

}