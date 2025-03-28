package com.xuhh.capybaraledger.data.repository

import com.xuhh.capybaraledger.data.dao.BillDao
import com.xuhh.capybaraledger.data.model.Bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BillRepository(private val billDao: BillDao) {

    // 插入账单
    suspend fun createBill(bill: Bill) = withContext(Dispatchers.IO) {
        billDao.insert(bill)
    }

    // 根据日期和账本 ID 获取账单
    suspend fun getBillsByDate(date: String, ledgerId: Long?) = withContext(Dispatchers.IO) {
    }

    // 根据月份和账本 ID 获取账单
    suspend fun getBillsByLedgerAndMonth(ledgerId: Long, startDate: String, endDate: String) =
        withContext(Dispatchers.IO) {
        }

    // 删除账单
    suspend fun deleteBill(id: Long) = withContext(Dispatchers.IO) {
        billDao.deleteBill(id)
    }

    // 更新账单
    suspend fun updateBill(bill: Bill) = withContext(Dispatchers.IO) {
        billDao.updateBill(bill)
    }

    // 获取支出金额统计
    suspend fun getExpenseAmount(type: Int, startDate: String, endDate: String) =
        withContext(Dispatchers.IO) {
        }


}