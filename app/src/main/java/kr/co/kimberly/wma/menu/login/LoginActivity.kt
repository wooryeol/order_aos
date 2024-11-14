package kr.co.kimberly.wma.menu.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.chrisbanes.photoview.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupSingleMessage
import kr.co.kimberly.wma.databinding.ActLoginBinding
import kr.co.kimberly.wma.menu.main.MainActivity
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActLoginBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var mAgencyCode: String? = null // 대리점 코드
    private var mPhoneNumber : String? = null // 연락처

    private val mPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Manifest.permission.READ_PHONE_NUMBERS
    } else {
        Manifest.permission.READ_PRECISE_PHONE_STATE
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        // 대리점 코드와 휴대폰 번호
        mAgencyCode = SharedData.getSharedData(mContext, "agencyCode", "")
        mPhoneNumber = SharedData.getSharedData(mContext, "phoneNumber", "")

        // 저장되어 있는 전화번호가 없으면 가져올 수 있게 세팅
        if (mPhoneNumber == "") {
            requestPhoneNumPermission()
        }

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
            mBinding.etId.setText("c000000")
            mBinding.etPw.setText("@mirae2024")
        }

        val loginTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isLogin()) {
                    mBinding.btLogin.setBackgroundResource(R.drawable.bt_round_1d6de5) // 활성화 상태 색상
                } else {
                    mBinding.btLogin.setBackgroundResource(R.drawable.bt_round_c9cbd0) // 비활성화 상태 색상
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        if (isLogin()) {
            mBinding.btLogin.setBackgroundResource(R.drawable.bt_round_1d6de5) // 활성화 상태 색상
        } else {
            mBinding.btLogin.setBackgroundResource(R.drawable.bt_round_c9cbd0) // 비활성화 상태 색상
        }

        mBinding.etId.addTextChangedListener(loginTextWatcher)
        mBinding.etPw.addTextChangedListener(loginTextWatcher)

        mBinding.etPw.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mBinding.btLogin.performClick()
                true
            } else {
                false
            }
        }

    }

    private fun isLogin(): Boolean{
        val idInput = mBinding.etId.text.toString().trim()
        val pwInput = mBinding.etPw.text.toString().trim()
        val isInputValid = idInput.isNotEmpty() && pwInput.isNotEmpty()

        mBinding.btLogin.isEnabled = isInputValid
        return mBinding.btLogin.isEnabled
    }

    private fun loginCheck() {
        if (mAgencyCode == null || mPhoneNumber == null || mAgencyCode == "" || mPhoneNumber == "") {
            Utils.popupNotice(this, "환경설정에서 대리점코드 혹은 휴대폰 번호를 확인해주세요")
        } else {
            if (mBinding.etId.text.isNotEmpty() && mBinding.etPw.text.isNotEmpty()) {

                // 로그인
                login()

                if (mBinding.ivCheck.isSelected) {
                    // true면 값을 저장
                    SharedData.setSharedData(mContext, "loginId", mBinding.etId.text.toString())
                } else {
                    // false면 값을 초기화
                    SharedData.setSharedData(mContext, "loginId", "")
                }
            } else {
                Utils.popupNotice(this, "아이디 또는 비밀번호를 확인해주세요")
            }
        }
    }

    private fun login() {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val userId = mBinding.etId.text.toString()
        val userPw = mBinding.etPw.text.toString()

        val json = JsonObject().apply {
            addProperty("agencyCd", mAgencyCode)
            addProperty("userId", userId)
            addProperty("userPw", userPw)
            addProperty("mobileNo", mPhoneNumber)
        }

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())

        val call = service.postLogin(body)
        call.enqueue(object : retrofit2.Callback<ResultModel<List<LoginResponseModel>>> {
            override fun onResponse(
                call: Call<ResultModel<List<LoginResponseModel>>>,
                response: Response<ResultModel<List<LoginResponseModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    when (item?.returnCd) {
                        Define.RETURN_CD_00 -> {
                            Utils.log("login success\nreturn code: ${item.returnCd}\nreturn message: ${item.returnMsg}")
                            SharedData.setSharedData(mContext, SharedData.LOGIN_DATA, Gson().toJson(item.data))
                            val intent = Intent(mContext,  MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "01" -> {
                            Utils.popupNotice(mContext, "아이디, 비밀번호, 대리점코드 또는 전화번호를 다시 확인해주세요")

                        }
                        else -> {
                            Utils.popupNotice(mContext, item?.returnMsg!!)
                        }
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "로그인 정보를 확인해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<List<LoginResponseModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("login failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        mAgencyCode = SharedData.getSharedData(mContext, "agencyCode", "")
        mPhoneNumber = SharedData.getSharedData(mContext, "phoneNumber", "")
    }

    private fun checkPhoneNumPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                @SuppressLint("HardwareIds", "MissingPermission")
                override fun onPermissionGranted() {
                    val tm = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    if (tm != null) {
                        val phoneNum = tm.line1Number
                        if (phoneNum != null) {
                            mPhoneNumber = tm.line1Number
                            SharedData.setSharedData(mContext, "phoneNumber", tm.line1Number)
                        } else {
                            //Utils.popupNotice(mContext, "환경설정에서 휴대폰 번호를 설정해주세요")
                            Utils.popupNotice(mContext, "휴대폰 번호를 가져올 수 없습니다.")
                        }
                    } else {
                        //Utils.popupNotice(mContext, "환경설정에서 휴대폰 번호를 설정해주세요")
                        Utils.popupNotice(mContext, "권한을 허용해주세요.\n[설정] > [앱 및 알림] > [고급] > [앱 권한]")
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    //Utils.popupNotice(mContext, "환경설정에서 휴대폰 번호를 설정해주세요")
                    Utils.popupNotice(mContext, "권한을 허용해주세요.\n[설정] > [앱 및 알림] > [고급] > [앱 권한]")
                }
            })
            .setDeniedMessage("권한을 허용해주세요.\n[설정] > [앱 및 알림] > [고급] > [앱 권한]")
            .setPermissions(mPermission)
            .check()
    }

    private fun requestPhoneNumPermission(){
        if (ActivityCompat.checkSelfPermission(mContext, mPermission) != PackageManager.PERMISSION_GRANTED) {
            checkPhoneNumPermission()
        }
    }

    private var clickTime: Long = 0
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val current = System.currentTimeMillis()
        if (supportFragmentManager.backStackEntryCount == 0) {
            if(current - clickTime >= 2000) {
                PopupSingleMessage(mContext, "모바일 유한킴벌리를\n종료하시겠습니까?", null).show()
            } else {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }
}