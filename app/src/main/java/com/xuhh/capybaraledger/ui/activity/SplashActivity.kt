package com.xuhh.capybaraledger.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import com.xuhh.capybaraledger.MainActivity
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏颜色为开屏背景色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_background)

        // 设置初始状态
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f

        // 创建动画
        val logoAnimator = ObjectAnimator.ofFloat(binding.ivLogo, "alpha", 0f, 1f)
        val nameAnimator = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f)

        // 设置动画集合
        AnimatorSet().apply {
            playTogether(logoAnimator, nameAnimator)
            duration = 1000 // 动画时长1秒
            interpolator = LinearInterpolator()
            
            // 动画结束后等待1秒再跳转
            addListener(onEnd = {
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }, 1000) // 延迟1秒跳转
            })
            
            // 开始动画
            start()
        }
    }
} 