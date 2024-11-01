package kr.co.kimberly.wma.menu.setting

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.BuildConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.adapter.PairedDevicesAdapter
import kr.co.kimberly.wma.common.Bluetooth
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.databinding.ActSettingBinding
import kr.co.kimberly.wma.network.model.DevicesModel

class SettingActivity : AppCompatActivity() {

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            SharedData.setSharedData(mContext, "agencyCode", mBinding.accountCode.text.toString())
            SharedData.setSharedData(mContext, "phoneNumber", mBinding.mobileNumber.text.toString())
            finish()
        }
    }

    private lateinit var mBinding: ActSettingBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var mAgencyCode: String? = null // 대리점 코드
    private var mPhoneNumber : String? = null // 연락처

    private val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
    }
    else {
        arrayOf(
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSettingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        if (!BuildConfig.DEBUG) {
            mBinding.mobileNumber.text = "01029812904"
            mBinding.accountCode.setText("C000000")
        }

        this.onBackPressedDispatcher.addCallback(this, callback)

        // 헤더 설정 변경
        mBinding.header.scanBtn.visibility = View.GONE
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                // 대리점 코드 및 휴대폰 번호 저장
                SharedData.setSharedData(mContext, "agencyCode", mBinding.accountCode.text.toString())
                SharedData.setSharedData(mContext, "phoneNumber", mBinding.mobileNumber.text.toString())
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                requestPermission()
            }
        })

        //대리점 코드 및 전화번호 세팅
        getInfoSetting()
        //연결된 블루투스 목록
        showPairedList()

    }

    private fun getInfoSetting() {
        mAgencyCode = SharedData.getSharedData(mContext, "agencyCode", "")
        mPhoneNumber = SharedData.getSharedData(mContext, "phoneNumber", "")

        if (mAgencyCode != "") {
            mBinding.accountCode.setText(mAgencyCode.toString())
        }

        if (mPhoneNumber != "") {
            mBinding.mobileNumber.text = mPhoneNumber.toString()
        }
    }

    private fun showPairedList(){
        val adapter = PairedDevicesAdapter(mContext, mActivity)
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }

    private fun requestPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    showDeviceList()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(mContext, "권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                }
            })
            .setDeniedMessage("권한을 허용해주세요.\n[설정] > [앱 및 알림] > [고급] > [앱 권한]")
            .setPermissions(*ALL_BLE_PERMISSIONS)
            .check()
    }

    private fun showDeviceList(){
        val popup = PopupSearchDevices(mContext)
        if (mBinding.accountCode.text.isEmpty()) {
            Utils.popupNotice(mContext, "대리점 코드를 입력해주세요")
        }  else {
            popup.show()
        }
    }
}