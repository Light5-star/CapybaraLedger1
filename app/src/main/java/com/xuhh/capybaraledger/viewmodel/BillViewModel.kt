package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.xuhh.capybaraledger.data.database.AppDatabase
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.repository.BillRepository
import kotlinx.coroutines.launch

class BillViewModel(private val billRepository: BillRepository) : ViewModel() {
    private lateinit var database: AppDatabase
    private var defaultLedger: Ledger? = null
    private var currentLedger: Ledger? = defaultLedger

    init {

        viewModelScope.launch{
            setDefaultLedger()
        }
    }

    // 当前账本
    fun getCurrentLedger() = currentLedger

    private suspend fun setDefaultLedger() {
        defaultLedger = database.ledgerDao().getDefaultLedger()
    }
}