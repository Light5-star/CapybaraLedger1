package com.xuhh.capybaraledger.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Ledger
import com.xuhh.capybaraledger.data.repository.BillRepository
import com.xuhh.capybaraledger.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val _ledgers = MutableStateFlow<List<Ledger>>(emptyList())
    val ledgers: StateFlow<List<Ledger>> = _ledgers.asStateFlow()

    init {
        loadLedgers()
        loadDefaultLedger()
    }

    private fun loadLedgers() {
        viewModelScope.launch {
            ledgerRepository.getAllLedgersFlow().collect { ledgers ->
                _ledgers.value = ledgers
            }
        }
    }

    private fun loadDefaultLedger() {
        viewModelScope.launch {
            val defaultLedger = ledgerRepository.getDefaultLedger()
            defaultLedger?.let { 
                updateCurrentLedger(it)
            }
        }
    }

    fun updateCurrentLedger(ledger: Ledger) {
        viewModelScope.launch {
            _currentLedger.value = ledger
            loadBillsForCurrentLedger()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadBillsForCurrentLedger() {
        viewModelScope.launch {
            val currentDate = getCurrentDate()
            val ledgerId = _currentLedger.value?.id ?: return@launch
            
            try {
                // 获取当日账单
                val startTimestamp = parseDateToTimestamp(currentDate)
                val endTimestamp = startTimestamp + 86400000 // 加1天时间戳
                
                // 加载账单
                val bills = billRepository.getBillsByDateRange(
                    ledgerId = ledgerId,
                    startDate = startTimestamp,
                    endDate = endTimestamp
                )
                _bills.value = bills
                
                // 计算余额
                calculateDailyBalance(ledgerId, startTimestamp, endTimestamp)
            } catch (e: Exception) {
                // 处理错误
                _bills.value = emptyList()
                _balance.value = 0.0
            }
        }
    }

    private suspend fun calculateDailyBalance(ledgerId: Long, startDate: Long, endDate: Long) {
        val income = billRepository.getAmountByType(ledgerId, Bill.TYPE_INCOME, startDate, endDate) ?: 0.0
        val expense = billRepository.getAmountByType(ledgerId, Bill.TYPE_EXPENSE, startDate, endDate) ?: 0.0
        _balance.value = income - expense
    }

    private fun parseDateToTimestamp(date: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(date)?.time ?: 0L
    }

    fun createLedger(name: String) {
        viewModelScope.launch {
            val ledger = Ledger(name = name)
            ledgerRepository.createLedger(ledger)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 获取指定月份的账单
    suspend fun getBillsByMonth(ledgerId: Long, startDate: String, endDate: String) = 
        billRepository.getBillsByLedgerAndMonth(ledgerId, startDate, endDate)

    // 获取指定日期的账单
    suspend fun getBillsByDate(date: String, ledgerId: Long?) = 
        billRepository.getBillsByDate(date, ledgerId)

    // 获取指定时间范围内的账单数据
    suspend fun getBillsWithCategoryByTimeRange(
        ledgerId: Long,
        startTime: Long,
        endTime: Long
    ): List<BillWithCategory> {
        return billRepository.getBillsWithCategoryByTimeRange(ledgerId, startTime, endTime)
    }
}