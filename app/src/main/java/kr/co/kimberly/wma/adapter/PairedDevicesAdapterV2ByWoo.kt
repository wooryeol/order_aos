package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.combine
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.databinding.HeaderSettingBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.model.DevicesModel
import kr.co.kimberly.wma.network.model.OrderRegModel
import java.util.ArrayList

class PairedDevicesAdapterV2ByWoo(context: Context, activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: List<BluetoothDevice> = ArrayList()
    var mContext = context
    var mActivity = activity

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(HeaderSettingBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellPairedDevicesBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(dataList[position - 1])
            }
            is HeaderViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = dataList.size + 1

    inner class ViewHolder(val binding: CellPairedDevicesBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
        fun bind(itemModel: BluetoothDevice) {
            binding.deviceName.text = itemModel.name
            binding.deviceAddress.text = itemModel.address

            // 기기에 따라 보여주는 아이콘 다르게
            if (itemModel.name.startsWith("Alpha") || SettingActivity.isRadioChecked == 2) {
                binding.deviceIcon.setImageResource(R.drawable.print)
            } else {
                binding.deviceIcon.setImageResource(R.drawable.adf_scanner)
            }
        }
    }

    inner class HeaderViewHolder(val binding: HeaderSettingBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("MissingPermission")
        fun bind() {
            // 데이터가 없으면 divide line 안보이도록
            if (dataList.isNotEmpty()){
                binding.bottomDivideLine.visibility = View.VISIBLE
            } else {
                binding.bottomDivideLine.visibility = View.GONE
            }
        }
    }
}