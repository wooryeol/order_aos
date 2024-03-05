package kr.co.kimberly.wma.menu.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.BuildConfig
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActLoginBinding
import kr.co.kimberly.wma.menu.main.MainActivity
import kr.co.kimberly.wma.menu.setting.SettingActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActLoginBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.btLogin.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                loginCheck()
            }
        })

        mBinding.settingBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                startActivity(Intent(mContext, SettingActivity::class.java))
            }
        })

        // 초기화 값이 아니면 저장된 아이디를 세팅 및 저장 버튼 slected로 변경
        val id = SharedData.getSharedData(mContext, "loginId", "")
        if (id != "") {
            mBinding.etId.setText(id)
            mBinding.ivCheck.isSelected = true
        }

        // 아이디 저장 버튼 누를 때마다 ivCheck의 isSelected 값 변경
        mBinding.llCheck.setOnClickListener {
            mBinding.ivCheck.isSelected = !mBinding.ivCheck.isSelected
        }

        // 앱버전
        mBinding.appVer.text = "ver ${BuildConfig.VERSION_NAME}"

        // 테트스 환경 로그인 정보 자동 기입
        if(Define.IS_TEST) {
            mBinding.etId.setText("developwoo")
            mBinding.etPw.setText("1234")
        }
    }

    private fun loginCheck() {
        if (mBinding.etId.text.isNotEmpty() && mBinding.etPw.text.isNotEmpty()) {
            if (mBinding.ivCheck.isSelected) {
                // true면 값을 저장
                SharedData.setSharedData(mContext, "loginId", mBinding.etId.text.toString())
            } else {
                // false면 값을 초기화
                SharedData.setSharedData(mContext, "loginId", "")
            }
            startActivity(Intent(mContext, MainActivity::class.java))
            finish()
        } else {
            PopupNotice(this, "아이디 또는 비밀번호를 확인해주세요").show()
        }
    }
}