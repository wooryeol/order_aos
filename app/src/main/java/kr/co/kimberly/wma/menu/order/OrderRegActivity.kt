package kr.co.kimberly.wma.menu.order

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.databinding.ActOrderRegBinding

class OrderRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
    }
}