package com.xuhh.capybaraledger.data.model

import androidx.annotation.StringRes
import com.xuhh.capybaraledger.R

/**
 * 类别模型
 * 类别的数据
 */
data class Category(
    val id: Long = 0,
    val name: String,
    @StringRes val icon: Int,
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
        Category(1, "餐饮", R.string.icon_canyin, 0, 1),
        Category(2, "零食", R.string.icon_lingshi, 0, 2),
        Category(3, "水果", R.string.icon_shuiguo, 0, 3),
        Category(4, "蔬菜", R.string.icon_shucai, 0, 4),
        Category(5, "饮品", R.string.icon_drink, 0, 5),
        Category(6, "日用品", R.string.icon_riyongpin, 0, 6),
        Category(7, "交通", R.string.icon_jiaotong_xinxi, 0, 7),
        Category(8, "住房", R.string.icon_zhufang, 0, 8),
        Category(9, "通讯", R.string.icon_dianhua, 0, 9),
        Category(10, "购物", R.string.icon_gouwuche, 0, 10),
        Category(11, "数码", R.string.icon_shuma_jiadian_leimu, 0, 11),
        Category(12, "医疗", R.string.icon_drink, 0, 12),
        Category(13, "教育", R.string.icon_jiaoyu, 0, 13),
        Category(14, "办公", R.string.icon_bangong, 0, 14),
        Category(15, "社交", R.string.social, 0, 15),
        Category(16, "娱乐", R.string.icon_fanyule, 0, 16),
        Category(17, "运动", R.string.icon_yundong, 0, 17),
        Category(18, "美容", R.string.icon_meirong, 0, 18),
        Category(19, "宠物", R.string.icon_chongwu, 0, 19),
        Category(20, "游戏", R.string.icon_youxi, 0, 20),
        Category(21, "汽车", R.string.icon_qiche_001, 0, 21),
        Category(22, "快递", R.string.icon_kuaidi, 0, 22),
        Category(23, "彩票", R.string.icon_caipiao, 0, 23),
        Category(24, "捐赠", R.string.icon_aixin_juanzeng, 0, 24),
        Category(25, "礼物", R.string.icon_liwulipin, 0, 25),
        Category(26, "其他支出", R.string.icon_category, 0, 26)
    )

    // 收入类别
    val INCOME_CATEGORIES = listOf(
        Category(101, "工资", R.string.icon_gongzi_jianyi, 1, 1),
        Category(102, "奖金", R.string.icon_jiangjin_guize, 1, 2),
        Category(103, "红包", R.string.icon_hongbao, 1, 3),
        Category(104, "理财收益", R.string.icon_licai, 1, 4),
        Category(105, "分红", R.string.icon_gudong_fenhong, 1, 5),
        Category(106, "其他收入", R.string.icon_category, 1, 6)
    )

    // 获取所有类别
    fun getAllCategories() = EXPENSE_CATEGORIES + INCOME_CATEGORIES

    // 根据类型获取类别
    fun getCategoriesByType(type: Int) = 
        if (type == 0) EXPENSE_CATEGORIES else INCOME_CATEGORIES

    // 根据ID获取类别
    fun getCategoryById(id: Long) = getAllCategories().find { it.id == id }

} 