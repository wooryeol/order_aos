package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.common.BluetoothCheck
import kr.co.kimberly.wma.databinding.PopupSearchDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.DevicesModel

open class PopupSearchDevices(private val mContext: AppCompatActivity, private val mActivity: Activity) {

    private lateinit var mBinding: PopupSearchDevicesBinding
    private val mDialog = Dialog(mContext)
    private val mBluetooth = BluetoothCheck(mContext, mActivity)
    open val adapter = SearchDevicesAdapter(mContext, mActivity)

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    fun show() {
        mBinding = PopupSearchDevicesBinding.inflate(mContext.layoutInflater)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setContentView(mBinding.root)
        mDialog.setCancelable(false)
        val height = Resources.getSystem().displayMetrics.heightPixels * 0.5
        mDialog.window?.setLayout(960, height.toInt())

        //SettingActivity.searchedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))

        adapter.dataList = SettingActivity.searchedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBluetooth.initBluetooth()

        mBinding.closeBtn.setOnClickListener {
            if (BluetoothCheck(mContext, mActivity).bluetoothAdapter.isDiscovering) {
                BluetoothCheck(mContext, mActivity).bluetoothAdapter.cancelDiscovery()
            }
            mDialog.dismiss()
        }

        mDialog.show()
    }
}