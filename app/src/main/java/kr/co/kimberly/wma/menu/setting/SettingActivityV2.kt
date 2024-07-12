package kr.co.kimberly.wma.menu.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupSearchDevicesV2
import kr.co.kimberly.wma.databinding.ActSettingV2Binding

class SettingActivityV2 : AppCompatActivity() {
    private lateinit var mBinding: ActSettingV2Binding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener

    private val bluetoothOnResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_CANCELED) {
            Utils.popupNotice(mContext, "블루투스를 켜야 사용 가능합니다.")
        }
    }

    /*companion object {
        // 스캐너 라디오 버튼을 선택하면 1, 프린터는 2
        // 팝업으로 넘겨주어 UI 변경
        var isRadioChecked = 0

        // 검색된 기기 목록을 담는 리스트
        val searchedList = ArrayList<BluetoothDevice>()

        // 연결된 기기 목록을 담는 리스트
        val pairedList = ArrayList<DevicesModel>()
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSettingV2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        radioGroupCheckedListener = OnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radioScanner -> {
                    // 각 항목을 선택하면 테두리 색 변경
                    mBinding.radioScannerBox.setBackgroundResource(R.drawable.et_round_1d6de5)
                    mBinding.radioPrintBox.setBackgroundResource(R.drawable.et_round_c9cbd0)
                    // isRadioChecked = 1
                    mBinding.radioPrint.isChecked = false
                }
                R.id.radioPrintBox -> {
                    mBinding.radioPrintBox.setBackgroundResource(R.drawable.et_round_1d6de5)
                    mBinding.radioScannerBox.setBackgroundResource(R.drawable.et_round_c9cbd0)
                    // isRadioChecked = 2
                    mBinding.radioScanner.isChecked = false
                }
            }
        }

        mBinding.radioGroup.setOnCheckedChangeListener(radioGroupCheckedListener)

        // 헤더 설정 변경
        mBinding.header.scanBtn.visibility = View.GONE
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.accountCode.setOnEditorActionListener { v, _, _ ->
            v.inputType = EditorInfo.TYPE_NULL
            true
        }

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupSearchDevicesV2 = PopupSearchDevicesV2(mContext, this@SettingActivityV2, bluetoothOnResultLauncher)
                popupSearchDevicesV2.show()
            }
        })

        // searchDevices()
        // showPairedDevices()
    }

    private fun showPairedDevices() {
        /*pairedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))
        pairedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))*/

        /*val adapter = PairedDevicesAdapter(mContext, mActivity)
        adapter.dataList = pairedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (pairedList.isNotEmpty()){
            mBinding.bottomDivideLine.visibility = View.VISIBLE
        } else {
            mBinding.bottomDivideLine.visibility = View.GONE
        }*/
    }

    /*private fun searchDevices(){
        mBinding.bottom.bottomButton.setOnClickListener {
            val mBluetooth = BluetoothCheck(this, mActivity)
            if (mBinding.accountCode.text.isEmpty()) {
                Toast.makeText(mContext, "대리점 코드를 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                when (isRadioChecked) {
                    1 -> {
                        val dlg = PopupSearchDevices(this, mActivity)
                        dlg.show()
                        Log.d("wooryeol", "스캐너 선택됨")
                        mBluetooth.searchBluetooth()
                    }
                    2 -> {
                        val dlg = PopupSearchDevices(this, mActivity)
                        dlg.show()
                        Log.d("wooryeol", "프린터 선택됨")
                        mBluetooth.searchBluetooth()
                    }
                    else -> {
                        Toast.makeText(mContext, "스캐너 혹은 프린터를 체크해주세요", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }*/
}