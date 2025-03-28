package com.xuhh.capybaraledger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.xuhh.capybaraledger.data.model.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: Bill)

    @Query("SELECT * FROM bills WHERE date = :date AND ledger_id = :ledgerId")
    suspend fun getBillsByDate(date: String, ledgerId: Long?): List<Bill>

    @Query("SELECT * FROM bills WHERE ledger_id = :ledgerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getBillsByLedgerAndMonth(ledgerId: Long, startDate: String, endDate: String): List<Bill>

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: Long)

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("SELECT SUM(amount) FROM bills WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseAmount(type: Int, startDate: String, endDate: String): Double

    @Query("SELECT * FROM bills")
    fun getAllBillsFlow(): Flow<List<Bill>>
}