package com.xuhh.capybaraledger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView

class BillAdapter(
    private val onBillClick: (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.ViewHolder>(BillDiffCallback) {

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
        val bill = getItem(position)

        // 设置分类图标
        val category = Categories.getAllCategories().find { it.name == bill.category }
        holder.tvIcon.text = (category?.icon ?: "\ue692").toString()

        // 设置分类名称和备注
        holder.tvCategory.text = bill.category
        if (bill.note.isNullOrEmpty()) {
            holder.tvNote.visibility = View.GONE
        } else {
            holder.tvNote.apply {
                visibility = View.VISIBLE
                text = bill.note
            }
        }

        // 设置金额
        holder.tvAmount.text = String.format(
            if (bill.type == 0) "-%.2f" else "+%.2f",
            bill.amount
        )

        // 设置点击事件
        holder.itemView.setOnClickListener { onBillClick(bill) }
    }

    object BillDiffCallback : DiffUtil.ItemCallback<Bill>() {
        override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
            return oldItem == newItem
        }
    }

    fun submitSortedList(newBills: List<Bill>) {
        Log.d("HomeBillListAdapter", "Submitting new list with ${newBills.size} bills")
        submitList(newBills.sortedByDescending { it.date })
    }
}
