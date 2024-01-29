package kr.co.kimberly.wma.menu.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.databinding.ActMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActMainBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
    }
}