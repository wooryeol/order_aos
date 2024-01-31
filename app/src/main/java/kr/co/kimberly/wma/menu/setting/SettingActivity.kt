package kr.co.kimberly.wma.menu.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PairedDevicesAdapter
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.databinding.ActSettingBinding
import kr.co.kimberly.wma.model.DevicesModel

class SettingActivity : AppCompatActivity() {

    companion object {
        // 스캐너 라디오 버튼을 선택하면 1, 프린터는 2
        // 팝업으로 넘겨주어 UI 변경
        var isRadioChecked = 0
        
        // 검색된 기기 목록을 담는 리스트
        val searchedList = ArrayList<DevicesModel>()

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

        searchDevices()
    }

    fun showPairedDevices() {
        pairedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91", true))

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
        mBinding.searchDeviceBtn.setOnClickListener {
            onRadioButtonClicked(mBinding.radioGroup)
            when (isRadioChecked) {
                1 -> {
                    val dlg = PopupSearchDevices(this, mActivity)
                    dlg.show()
                    Log.d("wooryeol", "스캐너 선택됨")
                }
                2 -> {
                    val dlg = PopupSearchDevices(this, mActivity)
                    dlg.show()
                    Log.d("wooryeol", "프린터 선택됨")
                }
                else -> {
                    Toast.makeText(mContext, "스캐너 혹은 프린터를 체크해주세요", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onRadioButtonClicked(view: View): Int {

        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                R.id.radioScanner ->
                    if (checked) {
                        isRadioChecked = 1
                        mBinding.radioPrint.isChecked = false
                        Log.d("wooryeol", "스캐너가 체크 되었습니다.")
                    }
                R.id.radioPrint ->
                    if (checked) {
                        isRadioChecked = 2
                        mBinding.radioScanner.isChecked = false
                        Log.d("wooryeol", "프린터가 체크 되었습니다.")
                }
            }
        }
        return isRadioChecked
    }
}