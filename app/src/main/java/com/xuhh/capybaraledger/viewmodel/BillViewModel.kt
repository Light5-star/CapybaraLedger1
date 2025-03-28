package com.xuhh.capybaraledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.repository.BillRepository
import kotlinx.coroutines.launch

class BillViewModel(private val billRepository: BillRepository) : ViewModel() {

    // 获取所有账单的 LiveData
    val allBills = billRepository.getAllBillsFlow().asLiveData()

    // 插入账单
    fun createBill(bill: Bill) {
        viewModelScope.launch {
            billRepository.createBill(bill)
        }
    }

    // 根据日期获取账单
    fun getBillsByDate(date: String, ledgerId: Long?) {
        viewModelScope.launch {
            val bills = billRepository.getBillsByDate(date, ledgerId)
            // 处理获取到的账单数据
        }
    }
}