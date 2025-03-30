package com.xuhh.capybaraledger.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Category

data class BillWithCategory(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category
)

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: Bill): Long

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Long): Bill?

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE date = :date AND ledger_id = :ledgerId
    """)
    suspend fun getBillsByDate(date: Long, ledgerId: Long?): List<BillWithCategory>

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE ledger_id = :ledgerId AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getBillsByLedgerAndMonth(ledgerId: Long, startDate: Long, endDate: Long): List<BillWithCategory>

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: Long)

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("SELECT SUM(amount) FROM bills WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseAmount(type: Int, startDate: Long, endDate: Long): Double

    @Transaction
    @Query("SELECT * FROM bills WHERE ledger_id = :ledgerId")
    suspend fun getBillsByLedger(ledgerId: Long): List<BillWithCategory>

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE date BETWEEN :startDate AND :endDate 
        AND ledger_id = :ledgerId
        ORDER BY date DESC
    """)
    suspend fun getDailyBills(
        startDate: Long,
        endDate: Long,
        ledgerId: Long
    ): List<BillWithCategory>

    @Query("SELECT * FROM bills WHERE ledger_id = :ledgerId AND date >= :startTime AND date <= :endTime ORDER BY date DESC")
    suspend fun getBillsByLedgerIdAndTimeRange(ledgerId: Long, startTime: Long, endTime: Long): List<Bill>
}