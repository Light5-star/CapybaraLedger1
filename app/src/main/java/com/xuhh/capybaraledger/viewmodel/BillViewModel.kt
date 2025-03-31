package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillViewModel(
    private val ledgerRepository: LedgerRepository,
    private val billRepository: BillRepository
) : ViewModel() {
    private val _currentLedger = MutableStateFlow<Ledger?>(null)
    val currentLedger: StateFlow<Ledger?> = _currentLedger.asStateFlow()

    private val _bills = MutableStateFlow<List<BillWithCategory>>(emptyList())
    val bills: StateFlow<List<BillWithCategory>> = _bills.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    fun updateCurrentLedger(ledger: Ledger) {
        _currentLedger.value = ledger
        loadBills(ledger.id)
        calculateBalance(ledger.id)
    }

    private fun loadBills(ledgerId: Long) {
        viewModelScope.launch {
            _bills.value = billRepository.getBillsByLedger(ledgerId)
        }
    }

    private fun calculateBalance(ledgerId: Long) {
        viewModelScope.launch {
            val (income, expense) = billRepository.getDailyBalance(ledgerId)
            _balance.value = income - expense
        }
    }
}