package kr.co.kimberly.wma.menu.setting

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.databinding.ActSettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var mBinding: ActSettingBinding
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSettingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
    }
}