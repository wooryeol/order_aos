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
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.common.BluetoothV2ByWoo
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

        val mBluetooth = BluetoothV2ByWoo(mContext, mActivity, searchedList, false)
        mBluetooth.checkBluetoothAvailable()

        mBinding.isPairing.playAnimation()

        // 재검색
        mBinding.retry.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBluetooth.checkBluetoothAvailable()
            }
        })

        // 로딩 애니메이션
        mBluetooth.bluetoothListener = object : BluetoothV2ByWoo.BluetoothListener{
            override fun hideLoadingImage() {
                mBinding.isPairing.visibility = View.INVISIBLE
            }

            override fun showLoadingImage() {
                mBinding.isPairing.visibility = View.VISIBLE
            }

            override fun onChangeAdapterData() {
                adapter.notifyDataSetChanged()
            }
        }

        adapter.dataList = searchedList

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.apply {
            reverseLayout = true
            stackFromEnd = true
        }
        mBinding.recyclerview.layoutManager = layoutManager

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            // 리스트가 추가될 때
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // layoutManager.scrollToPosition(0)
            }

            // 리스트가 update 될 때
            override fun onChanged() {
                super.onChanged()
                mBinding.recyclerview.scrollToPosition(adapter.dataList.size - 1)
            }
        })

        mBinding.recyclerview.adapter = adapter

        mBinding.closeBtn.setOnClickListener(object : OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                dismiss()
                if (mBluetooth.mBluetoothAdapter.isDiscovering) {
                    mBluetooth.mBluetoothAdapter.cancelDiscovery()
                }
                mContext.unregisterReceiver(mBluetooth.mBluetoothReceiver)
            }
        })

        adapter.itemClickListener = object : SearchDevicesAdapter.ItemClickListener {
            override fun onItemClick() {
                dismiss()
            }
        }
    }
}