package kr.co.kimberly.wma.menu.setting

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PairedDevicesAdapter
import kr.co.kimberly.wma.common.BluetoothCheck
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.databinding.ActSettingBinding
import kr.co.kimberly.wma.menu.main.MainActivity
import kr.co.kimberly.wma.model.DevicesModel

class SettingActivity : AppCompatActivity() {

    companion object {
        // 스캐너 라디오 버튼을 선택하면 1, 프린터는 2
        // 팝업으로 넘겨주어 UI 변경
        var isRadioChecked = 0
        
        // 검색된 기기 목록을 담는 리스트
        val searchedList = ArrayList<BluetoothDevice>()

        // 연결된 기기 목록을 담는 리스트
        val pairedList = ArrayList<DevicesModel>()
    }

    private lateinit var mBinding: ActSettingBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSettingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

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

        searchDevices()
        showPairedDevices()
    }

    private fun showPairedDevices() {
        /*pairedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))
        pairedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))*/

        val adapter = PairedDevicesAdapter(mContext, mActivity)
        adapter.dataList = pairedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (pairedList.isNotEmpty()){
            mBinding.bottomDivideLine.visibility = View.VISIBLE
        } else {
            mBinding.bottomDivideLine.visibility = View.GONE
        }
    }

    private fun searchDevices(){
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
                        mBluetooth.checkBluetooth()
                    }
                    2 -> {
                        val dlg = PopupSearchDevices(this, mActivity)
                        dlg.show()
                        Log.d("wooryeol", "프린터 선택됨")
                        mBluetooth.checkBluetooth()
                    }
                    else -> {
                        Toast.makeText(mContext, "스캐너 혹은 프린터를 체크해주세요", Toast.LENGTH_LONG).show()
                    }
                }
            }
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
                        Log.d("wooryeol", "스캐너가 체크 되었습니다.")
                    }
                R.id.radioPrint ->
                    if (checked) {
                        view.bringToFront()
                        mBinding.radioPrintBox.setBackgroundResource(R.drawable.et_round_1d6de5)
                        mBinding.radioScannerBox.setBackgroundResource(R.drawable.et_round_c9cbd0)
                        isRadioChecked = 2
                        mBinding.radioScanner.isChecked = false
                        Log.d("wooryeol", "프린터가 체크 되었습니다.")
                }
            }
        }
        return isRadioChecked
    }
}