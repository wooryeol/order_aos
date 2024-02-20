package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellSearchDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.DevicesModel
import java.util.ArrayList

class SearchDevicesAdapter(context: Context, activity: Activity): RecyclerView.Adapter<SearchDevicesAdapter.ViewHolder>() {

    var dataList: List<BluetoothDevice> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(val binding: CellSearchDevicesBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged", "MissingPermission")
        fun bind(itemModel: BluetoothDevice) {
            binding.deviceName.text = itemModel.name
            binding.deviceAddress.text = itemModel.address

            if (SettingActivity.isRadioChecked == 1) {
                binding.deviceIcon.setImageResource(R.drawable.adf_scanner)
            } else {
                binding.deviceIcon.setImageResource(R.drawable.print)
            }

            itemView.setOnClickListener {
                val paringDialog = PopupPairingDevice(mContext, mActivity)
                paringDialog.show(itemModel.name, itemModel.address)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchDevicesAdapter.ViewHolder {
        val binding = CellSearchDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: SearchDevicesAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}