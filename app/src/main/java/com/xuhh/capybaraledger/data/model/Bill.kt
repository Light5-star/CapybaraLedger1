package com.xuhh.capybaraledger.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey

/**
 * 账单记录模型
 */
@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = Ledger::class, // 关联的Ledger实体类
            parentColumns = ["id"],  // Ledger表的主键
            childColumns = ["ledger_id"], // 当前表的外键列，使用与@ColumnInfo相同的列名
            onDelete = ForeignKey.CASCADE // 当Ledger被删除时，级联删除相关的Bill记录
        )
    ]
)
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo("ledger_id")
    val ledgerId: Long,          // 所属账本ID
    val category: String,        // 分类
    val amount: Double,          // 金额
    val type: Int,              // 类型：0支出，1收入
    @ColumnInfo("date")
    val date: Long,  // 使用时间戳存储
    @ColumnInfo("time")
    val time: Long,  // 使用时间戳存储
    val note: String? = null,    // 备注
    val payee: String? = null    // 收/付款对象
) {
    companion object {
        const val TYPE_EXPENSE = 0  // 支出类型常量
        const val TYPE_INCOME = 1   // 收入类型常量
    }
} 