package com.xuhh.capybaraledger.ui.view.ledgerselect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.databinding.ItemLedgerSelectorBinding

class LedgerSelectorAdapter(
    private val onLedgerClick: (Ledger) -> Unit
) : ListAdapter<Ledger, LedgerSelectorAdapter.ViewHolder>(LedgerDiffCallback()) {

    inner class ViewHolder(private val binding: ItemLedgerSelectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ledger: Ledger, isSelected: Boolean) {
            binding.apply {
                tvName.text = ledger.name
                tvSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
                root.setOnClickListener {
                    val position = getAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        onLedgerClick(getItem(position))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLedgerSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ledger = getItem(position)
        holder.bind(ledger, ledger.isDefault)
    }

    private class LedgerDiffCallback : DiffUtil.ItemCallback<Ledger>() {
        override fun areItemsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ledger, newItem: Ledger): Boolean {
            return oldItem == newItem
        }
    }
} 