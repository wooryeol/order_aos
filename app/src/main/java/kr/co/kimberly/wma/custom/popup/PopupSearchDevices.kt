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
import kr.co.kimberly.wma.common.Bluetooth
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupSearchDevicesBinding
import kr.co.kimberly.wma.network.model.DevicesModel

@SuppressLint("NotifyDataSetChanged", "MissingPermission")
class PopupSearchDevices(private val mContext: Context): Dialog(mContext) {

    private lateinit var mBinding: PopupSearchDevicesBinding
    private lateinit var adapter: SearchDevicesAdapter
    private lateinit var mBluetooth: Bluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchDevicesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initView()

        // 블루투스 검색
        mBluetooth = Bluetooth(mContext) { item ->
            adapter.dataList = item
            adapter.notifyDataSetChanged()
        }
        mBluetooth.startScan()

        //어댑터 설정
        setAdapter()

        // 로딩 애니메이션
        setAnimation()

        // 재검색
        mBinding.retry.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBluetooth.startScan()
            }
        })
        // 닫기 버튼
        mBinding.closeBtn.setOnClickListener(object : OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                dismiss()
                mBluetooth.stopScan()
            }
        })

        adapter.itemClickListener = object : SearchDevicesAdapter.ItemClickListener {
            override fun onItemClick() {
                mBluetooth.stopScan()
                dismiss()
            }
        }

    }

    private fun initView() {
        setCancelable(false)
        val height = Resources.getSystem().displayMetrics.heightPixels * 0.5
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, height.toInt())
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setAdapter(){
        adapter = SearchDevicesAdapter(mContext)
        val layoutManager = LinearLayoutManager(mContext)
        mBinding.recyclerview.adapter = adapter
        layoutManager.apply {
            reverseLayout = true
            stackFromEnd = true
        }
        mBinding.recyclerview.layoutManager = layoutManager

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            // 리스트가 추가될 때
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // layoutManager.scrollToPosition(0)
                adapter.notifyDataSetChanged()

            }

            // 리스트가 update 될 때
            override fun onChanged() {
                super.onChanged()
                mBinding.recyclerview.scrollToPosition(adapter.dataList.size - 1)
            }
        })
    }

    private fun setAnimation(){
        //로딩바 애니메이션
        mBinding.isPairing.playAnimation()

        mBluetooth.bluetoothListener = object : Bluetooth.BluetoothListener{
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
    }
}