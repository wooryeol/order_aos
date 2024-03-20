package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.common.BluetoothV2
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupSearchDevicesBinding

class PopupSearchDevices(private val mContext: Context, private val mActivity: Activity): Dialog(mContext) {

    private lateinit var mBinding: PopupSearchDevicesBinding
    private val searchedList : ArrayList<BluetoothDevice> = ArrayList()
    val adapter = SearchDevicesAdapter(mContext, mActivity)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchDevicesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initView()
    }

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    private fun initView() {
        setCancelable(false)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val height = Resources.getSystem().displayMetrics.heightPixels * 0.5
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())

        val mBluetooth = BluetoothV2(mContext, mActivity, searchedList, adapter, false, mBinding.isPairing)

        mBinding.retry.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBluetooth.checkBluetoothAvailable()
            }
        })
        mBluetooth.checkBluetoothAvailable()

        adapter.dataList = searchedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        // 여기 대리님께 물어보기
        mBinding.recyclerview.scrollToPosition(adapter.dataList.size)
        mBinding.isPairing.playAnimation()

        mBinding.closeBtn.setOnClickListener {
            dismiss()
            if (mBluetooth.mBluetoothAdapter.isDiscovering) {
                mBluetooth.mBluetoothAdapter.cancelDiscovery()
            }
            mContext.unregisterReceiver(mBluetooth.mBluetoothReceiver)
        }
    }
}