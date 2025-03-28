package com.xuhh.capybaraledger.data.model

import androidx.annotation.StringRes
import com.xuhh.capybaraledger.R
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 类别模型
 * 类别的数据
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_INCOME = 1
    }
}

object Categories {
    // 支出类别
    val EXPENSE_CATEGORIES = listOf(
        Category(1, "餐饮", 0, 1),
        Category(2, "零食", 0, 2),
        Category(3, "水果", 0, 3),
        Category(4, "蔬菜", 0, 4),
        Category(5, "饮品", 0, 5),
        Category(6, "日用品", 0, 6),
        Category(7, "交通", 0, 7),
        Category(8, "住房", 0, 8),
        Category(9, "通讯", 0, 9),
        Category(10, "购物", 0, 10),
        Category(11, "数码", 0, 11),
        Category(12, "医疗", 0, 12),
        Category(13, "教育", 0, 13),
        Category(14, "办公", 0, 14),
        Category(15, "社交", 0, 15),
        Category(16, "娱乐", 0, 16),
        Category(17, "运动", 0, 17),
        Category(18, "美容", 0, 18),
        Category(19, "宠物", 0, 19),
        Category(20, "游戏", 0, 20),
        Category(21, "汽车", 0, 21),
        Category(22, "快递", 0, 22),
        Category(23, "彩票", 0, 23),
        Category(24, "捐赠", 0, 24),
        Category(25, "礼物", 0, 25),
        Category(26, "其他支出", 0, 26)
    )

    // 收入类别
    val INCOME_CATEGORIES = listOf(
        Category(101, "工资", 1, 1),
        Category(102, "奖金", 1, 2),
        Category(103, "红包", 1, 3),
        Category(104, "理财收益", 1, 4),
        Category(105, "分红", 1, 5),
        Category(106, "其他收入", 1, 6)
    )

    // 获取所有类别
    fun getAllCategories() = EXPENSE_CATEGORIES + INCOME_CATEGORIES

    // 根据类型获取类别
    fun getCategoriesByType(type: Int) = 
        if (type == 0) EXPENSE_CATEGORIES else INCOME_CATEGORIES

    // 根据ID获取类别
    fun getCategoryById(id: Long) = getAllCategories().find { it.id == id }

} 