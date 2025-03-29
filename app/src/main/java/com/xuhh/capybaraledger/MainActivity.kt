package com.xuhh.capybaraledger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xuhh.capybaraledger.databinding.ActivityMainBinding
import com.xuhh.capybaraledger.ui.activity.bill_edit_activity.BillEditActivity
import com.xuhh.capybaraledger.ui.base.BaseActivity
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView

class MainActivity : BaseActivity<ActivityMainBinding>(){
    private val billEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 刷新所有数据
            refreshAllData()
        }
    }

    private fun refreshAllData() {
        // 刷新明细页面
        supportFragmentManager.findFragmentByTag("details")?.let { fragment ->
        }
    }

    override fun initBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        settingBottomNav()
    }

    private fun settingBottomNav() {
        val bottomNavigationView = mBinding?.navView
        // 为每个菜单项设置自定义图标
        if (bottomNavigationView != null) {
            bottomNavigationView.menu.apply {
                findItem(R.id.navigation_home).icon = createFontIcon(R.string.icon_home)
                findItem(R.id.navigation_details).icon = createFontIcon(R.string.icon_mingxi)
                findItem(R.id.navigation_add).icon = createFontIcon(R.string.icon_chuangjian_tianjia_piliang_tianjia)
                findItem(R.id.navigation_statistics).icon = createFontIcon(R.string.icon_tubiao)
                findItem(R.id.navigation_profile).icon = createFontIcon(R.string.icon_wode)
            }
        }

        val navView: BottomNavigationView = mBinding?.navView!!
        // 获取 NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // 获取 NavController
        val navController = navHostFragment.navController

        // 设置底部导航与导航控制器的关联
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_details -> {
                    navController.navigate(R.id.navigation_details)
                    true
                }
                R.id.navigation_add -> {
                    // 打开记账界面
                    startBillEdit()
                    false  // 返回false表示不切换选中状态
                }
                R.id.navigation_statistics -> {
                    navController.navigate(R.id.navigation_statistics)
                    true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }

        // 设置默认选中的页面
        navController.navigate(R.id.navigation_home)
    }

    private fun createFontIcon(stringRes: Int): BitmapDrawable {
        val iconView = UnicodeTextView(this)
        iconView.text = getString(stringRes)
        iconView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        iconView.setTextColor(ContextCompat.getColor(this, R.color.text_primary))

        // 测量并布局 View
        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        iconView.measure(widthSpec, heightSpec)
        iconView.layout(0, 0, iconView.measuredWidth, iconView.measuredHeight)

        // 创建 Bitmap并绘制
        val bitmap = Bitmap.createBitmap(iconView.measuredWidth, iconView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        iconView.draw(canvas)

        return BitmapDrawable(resources, bitmap)
    }

    // 修改启动记账页面的方法
    fun startBillEdit(ledgerId: Long = -1L, selectedDate: Long = -1L) {
        val intent = Intent(this, BillEditActivity::class.java).apply {
            putExtra("ledger_id", ledgerId)
            putExtra("selected_date", selectedDate)
        }
        billEditLauncher.launch(intent)
    }

}