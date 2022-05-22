package com.example.moodleLearning.data.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodleLearning.databinding.CategoryItemBinding
import com.example.moodleLearning.data.models.Category

class CategoriesAdapter(val context: Context, val click: OnClick) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    private var list = mutableListOf<Category>()

    class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cat = list[position]

        holder.binding.tvName.text = cat.name

        holder.binding.root.setOnClickListener {
            click.onClickCategory(cat)
        }
    }

    fun setData(data: List<Category>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        list.clear()
        notifyDataSetChanged()
    }

    interface OnClick {
        fun onClickCategory(category: Category)
    }

}