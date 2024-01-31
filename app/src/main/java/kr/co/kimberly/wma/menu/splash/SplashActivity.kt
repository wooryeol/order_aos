package kr.co.kimberly.wma.menu.splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.databinding.ActSplashBinding
import kr.co.kimberly.wma.menu.login.LoginActivity
import kr.co.kimberly.wma.menu.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSplashBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(Intent(mContext, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}