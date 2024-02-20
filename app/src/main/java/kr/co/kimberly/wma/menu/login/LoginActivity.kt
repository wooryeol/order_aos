package kr.co.kimberly.wma.menu.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.databinding.ActLoginBinding
import kr.co.kimberly.wma.menu.main.MainActivity
import kr.co.kimberly.wma.menu.setting.SettingActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActLoginBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.btLogin.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                startActivity(Intent(mContext, MainActivity::class.java))
            }
        })

        mBinding.settingBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                startActivity(Intent(mContext, SettingActivity::class.java))
            }
        })
    }
}