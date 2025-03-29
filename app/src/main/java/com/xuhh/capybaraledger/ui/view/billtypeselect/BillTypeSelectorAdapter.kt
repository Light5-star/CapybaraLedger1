package com.xuhh.capybaraledger.ui.view.billtypeselect

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Category

class BillTypeSelectorAdapter(
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<BillTypeSelectorAdapter.ViewHolder>() {

    private var selectedPosition = -1
    private var categories: List<Category> = emptyList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcon: TextView = itemView.findViewById(R.id.tv_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill_type_selector, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]
//        holder.tvIcon.text = category.icon.toString()
        holder.tvName.text = category.name

        // 设置选中状态
        val isSelected = position == selectedPosition
        holder.itemView.isSelected = isSelected
        holder.tvIcon.isSelected = isSelected
        holder.tvName.isSelected = isSelected

        holder.itemView.setOnClickListener {
            if (position != selectedPosition) {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size

    fun setCategories(newCategories: List<Category>, selectedCategory: Category? = null) {
        categories = newCategories
        selectedPosition = categories.indexOfFirst { it.id == selectedCategory?.id }
        notifyDataSetChanged()
    }
} 