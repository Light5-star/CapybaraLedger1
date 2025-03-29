package com.xuhh.capybaraledger.data.model

import androidx.annotation.StringRes
import com.xuhh.capybaraledger.R
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 类别模型
 * 类别的数据
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: Int
) {
    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_INCOME = 1
    }
}

object Categories {
    // 支出类别
    val EXPENSE_CATEGORIES = listOf(
        Category(name = "餐饮", type = 0),
        Category(name = "零食", type = 0),
        Category(name = "水果", type = 0),
        Category(name = "蔬菜", type = 0),
        Category(name = "饮品", type = 0),
        Category(name = "日用品", type = 0),
        Category(name = "交通", type = 0),
        Category(name = "住房", type = 0),
        Category(name = "通讯", type = 0),
        Category(name =  "购物", type = 0),
        Category(name =  "数码", type = 0),
        Category(name =  "医疗", type = 0),
        Category(name =  "教育", type = 0),
        Category(name =  "办公", type = 0),
        Category(name =  "社交", type = 0),
        Category(name =  "娱乐", type = 0),
        Category(name =  "运动", type = 0),
        Category(name =  "美容", type = 0),
        Category(name =  "宠物", type = 0),
        Category(name =  "游戏", type = 0),
        Category(name =  "汽车", type = 0),
        Category(name =  "快递", type = 0),
        Category(name =  "彩票", type = 0),
        Category(name =  "捐赠", type = 0),
        Category(name =  "礼物", type = 0),
        Category(name =  "其他支出", type = 0)
    )

    // 收入类别
    val INCOME_CATEGORIES = listOf(
        Category(name =  "工资", type = 1),
        Category(name =  "奖金", type = 1),
        Category(name =  "红包", type = 1),
        Category(name =  "理财收益", type = 1),
        Category(name =  "分红", type = 1),
        Category(name =  "其他收入", type = 1)
    )

    // 获取所有类别
    fun getAllCategories() = EXPENSE_CATEGORIES + INCOME_CATEGORIES

    // 根据类型获取类别
    fun getCategoriesByType(type: Int) = 
        if (type == 0) EXPENSE_CATEGORIES else INCOME_CATEGORIES

    // 根据ID获取类别
    fun getCategoryById(id: Long) = getAllCategories().find { it.id == id }

} 