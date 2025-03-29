package com.xuhh.capybaraledger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView
import java.util.Date
import java.util.Locale

class BillAdapter(
    private val onBillClick: (BillWithCategory) -> Unit
) : ListAdapter<BillWithCategory, BillAdapter.ViewHolder>(BillDiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcon: UnicodeTextView = view.findViewById(R.id.tv_icon)
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val tvNote: TextView = view.findViewById(R.id.tv_note)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val billWithCategory = getItem(position)

        // 设置分类图标
        holder.tvIcon.setText(billWithCategory.category.iconResId)
        holder.tvCategory.text = billWithCategory.category.name

        // 设置分类名称和备注
        if (billWithCategory.bill.note.isNullOrEmpty()) {
            holder.tvNote.visibility = View.GONE
        } else {
            holder.tvNote.apply {
                visibility = View.VISIBLE
                text = billWithCategory.bill.note
            }
        }

        // 设置金额
        holder.tvAmount.text = String.format(
            if (billWithCategory.bill.type == 0) "-%.2f" else "+%.2f",
            billWithCategory.bill.amount
        )

        // 设置点击事件
        holder.itemView.setOnClickListener { onBillClick(billWithCategory) }

        // 根据收支类型设置不同颜色
        holder.tvAmount.setTextColor(
            if (billWithCategory.bill.type == 0) {
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
            } else {
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            }
        )
    }

    object BillDiffCallback : DiffUtil.ItemCallback<BillWithCategory>() {
        override fun areItemsTheSame(
            oldItem: BillWithCategory,
            newItem: BillWithCategory
        ): Boolean {
            return oldItem.bill.id == newItem.bill.id
        }

        override fun areContentsTheSame(
            oldItem: BillWithCategory,
            newItem: BillWithCategory
        ): Boolean {
            return oldItem == newItem
        }

    }

    fun submitSortedList(newBills: List<BillWithCategory>) {
        submitList(newBills.sortedByDescending { it.bill.date })
    }
}
