package com.xuhh.capybaraledger.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.xuhh.capybaraledger.data.dao.BillDao
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Bill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class BillRepository(private val billDao: BillDao) {

    // 插入账单
    suspend fun createBill(bill: Bill) = withContext(Dispatchers.IO) {
        billDao.insert(bill)
    }

    // 根据日期和账本 ID 获取账单
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBillsByDate(date: String, ledgerId: Long?) = withContext(Dispatchers.IO) {
        val timestamp = parseDateString(date)
        billDao.getBillsByDate(timestamp, ledgerId)
    }

    // 根据月份和账本 ID 获取账单
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getBillsByLedgerAndMonth(ledgerId: Long, startDate: String, endDate: String) =
        withContext(Dispatchers.IO) {
            val startTimestamp = parseDateString(startDate)
            val endTimestamp = parseDateString(endDate)
            billDao.getBillsByLedgerAndMonth(ledgerId, startTimestamp, endTimestamp)
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
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getExpenseAmount(type: Int, startDate: String, endDate: String) =
        withContext(Dispatchers.IO) {
            val startTimestamp = parseDateString(startDate)
            val endTimestamp = parseDateString(endDate)
            billDao.getExpenseAmount(type, startTimestamp, endTimestamp)
        }

    //将 "yyyy-MM-dd" 格式字符串转为时间戳
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseDateString(dateStr: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(dateStr, formatter)
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    suspend fun getBillsByLedger(ledgerId: Long): List<BillWithCategory> {
        return billDao.getBillsByLedger(ledgerId)
    }

    suspend fun getDailyBalance(ledgerId: Long): Pair<Double, Double> {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val startTimestamp = parseDateToTimestamp(currentDate)
        val endTimestamp = startTimestamp + 86400000

        val income = billDao.getExpenseAmount(
            type = Bill.TYPE_INCOME,
            startDate = startTimestamp,
            endDate = endTimestamp
        ) ?: 0.0

        val expense = billDao.getExpenseAmount(
            type = Bill.TYPE_EXPENSE,
            startDate = startTimestamp,
            endDate = endTimestamp
        ) ?: 0.0

        return Pair(income, expense)
    }

    private fun parseDateToTimestamp(date: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(date)?.time ?: 0L
    }

    // 获取指定日期范围内的账单
    suspend fun getBillsByDateRange(
        ledgerId: Long,
        startDate: Long,
        endDate: Long
    ): List<BillWithCategory> = withContext(Dispatchers.IO) {
        billDao.getBillsByDateRange(
            ledgerId = ledgerId,
            startDate = startDate,
            endDate = endDate
        )
    }

    // 获取指定类型的金额总和
    suspend fun getAmountByType(
        ledgerId: Long,
        type: Int,
        startDate: Long,
        endDate: Long
    ): Double = withContext(Dispatchers.IO) {
        billDao.getExpenseAmount(
            type = type,
            startDate = startDate,
            endDate = endDate,
            ledgerId = ledgerId
        )
    }

    // 获取指定时间范围内的账单数据
    suspend fun getBillsWithCategoryByTimeRange(
        ledgerId: Long,
        startTime: Long,
        endTime: Long
    ): List<BillWithCategory> = withContext(Dispatchers.IO) {
        billDao.getBillsWithCategoryByTimeRange(ledgerId, startTime, endTime)
    }
}