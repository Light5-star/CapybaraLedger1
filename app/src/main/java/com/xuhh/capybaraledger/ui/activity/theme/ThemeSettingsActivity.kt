package com.xuhh.capybaraledger.ui.activity.theme

import com.xuhh.capybaraledger.databinding.ActivityThemeSettingsBinding
import com.xuhh.capybaraledger.ui.base.BaseActivity

class ThemeSettingsActivity : BaseActivity<ActivityThemeSettingsBinding>() {
    override fun initBinding(): ActivityThemeSettingsBinding {
        return ActivityThemeSettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        mBinding.btnBack.setOnClickListener {
            finish()
        }
    }
} 