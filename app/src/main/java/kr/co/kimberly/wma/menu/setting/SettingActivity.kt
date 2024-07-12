package kr.co.kimberly.wma.menu.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PairedDevicesAdapterV2ByWoo
import kr.co.kimberly.wma.common.BluetoothV2ByWoo
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.databinding.ActSettingV3ByWooBinding
import kr.co.kimberly.wma.network.model.LoginResponseModel

class SettingActivity : AppCompatActivity() {
    companion object {
        // 스캐너 라디오 버튼을 선택하면 1, 프린터는 2
        // 팝업으로 넘겨주어 UI 변경
        var isRadioChecked = 0

        // 연결된 기기 목록을 담는 리스트
        val pairedList : ArrayList<BluetoothDevice> = ArrayList()
        val searchedList: ArrayList<BluetoothDevice> = ArrayList()

        var checkScanner = false
        var checkPrinter = false
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            SharedData.setSharedData(mContext, "agencyCode", mBinding.accountCode.text.toString())
            SharedData.setSharedData(mContext, "phoneNumber", mBinding.mobileNumber.text.toString())
            finish()
        }
    }

    //private lateinit var mBinding: ActSettingBinding
    private lateinit var mBinding: ActSettingV3ByWooBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    //private var adapter : PairedDevicesAdapter? = null
    private var adapter : PairedDevicesAdapterV2ByWoo? = null

    private var mAgencyCode: String? = null // 대리점 코드
    private var mPhoneNumber : String? = null // 연락처

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSettingV3ByWooBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

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
                searchDevices()
            }

        })

        getInfo()
        showPairedDevices()
        //useDevice()

    }

    @SuppressLint("MissingPermission")
    private fun showPairedDevices() {
        val mBluetooth = BluetoothV2ByWoo(mContext, mActivity, pairedList, true)
        mBluetooth.checkBluetoothAvailable()
        mBluetooth.bluetoothListener = object : BluetoothV2ByWoo.BluetoothListener{
            override fun hideLoadingImage() {
            }

            override fun showLoadingImage() {
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onChangeAdapterData() {
                adapter?.notifyDataSetChanged()
            }
        }
        adapter = PairedDevicesAdapterV2ByWoo(mContext, mActivity)
        adapter?.dataList = pairedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchDevices(){
        mBinding.bottom.bottomButton.setOnClickListener {
            val dlg = PopupSearchDevices(mContext, mActivity)
            if (mBinding.accountCode.text.isEmpty()) {
                Utils.popupNotice(mContext, "대리점 코드를 입력해주세요")
            } else if(isRadioChecked == 0) {
                Utils.popupNotice(mContext, "스캐너 또는 프린트를 선택해주세요")
            } else {
                when (isRadioChecked) {
                    1 -> {
                        dlg.show()
                        Utils.Log("scanner is selected")
                    }
                    2 -> {
                        dlg.show()
                        Utils.Log("printer is selected")
                    }
                }
            }
            val mBluetooth = BluetoothV2ByWoo(mContext, mActivity, searchedList, false)
            mBluetooth.checkBluetoothAvailable()
        }
    }

    fun onSettingActRadioButtonClicked(view: View): Int {
        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                R.id.radioScanner ->
                    if (checked) {
                        // 뷰의 맨 앞으로 보내서 사용자가 클릭하기 쉽도록
                        view.bringToFront()
                        // 각 항목을 선택하면 테두리 색 변경
                        mBinding.radioScannerBox.setBackgroundResource(R.drawable.et_round_1d6de5)
                        mBinding.radioPrintBox.setBackgroundResource(R.drawable.et_round_c9cbd0)
                        isRadioChecked = 1
                        mBinding.radioPrint.isChecked = false
                        Utils.Log("scanner is checked")
                    }
                R.id.radioPrint ->
                    if (checked) {
                        view.bringToFront()
                        mBinding.radioPrintBox.setBackgroundResource(R.drawable.et_round_1d6de5)
                        mBinding.radioScannerBox.setBackgroundResource(R.drawable.et_round_c9cbd0)
                        isRadioChecked = 2
                        mBinding.radioScanner.isChecked = false
                        Utils.Log("printer is checked")
                }
            }
        }
        return isRadioChecked
    }

    private fun useDevice() {
        val print = mBinding.checkBoxPrint
        val scanner = mBinding.checkBoxScanner

        scanner.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                scanner.isEnabled = false
                val name = SharedData.getSharedData(mContext, SharedData.SCANNER_NAME, "")
                val address = SharedData.getSharedData(mContext, SharedData.SCANNER_ADDR, "")
                if (address == "" && name == "") {
                    Utils.popupNotice(mContext, "스캐너를 연결해주세요")
                } else {
                    scanner.isEnabled = true
                    checkScanner = scanner.isChecked
                }
            }
        })

        print.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                print.isEnabled = false
                val name = SharedData.getSharedData(mContext, SharedData.PRINTER_NAME, "")
                val address = SharedData.getSharedData(mContext, SharedData.PRINTER_ADDR, "")
                if (address == "" && name == "") {
                    Utils.popupNotice(mContext, "프린터를 연결해주세요")
                } else {
                    print.isEnabled = true
                    checkPrinter = print.isChecked
                }
            }
        })
    }
    private fun getInfo() {
        mAgencyCode = SharedData.getSharedData(mContext, "agencyCode", "")
        mPhoneNumber = SharedData.getSharedData(mContext, "phoneNumber", "")

        if (mAgencyCode != "") {
            mBinding.accountCode.setText(mAgencyCode.toString())
        }

        if (mPhoneNumber != "") {
            mBinding.mobileNumber.setText(mPhoneNumber.toString())
        }
    }
}