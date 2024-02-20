package kr.co.kimberly.wma.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.DevicesModel
import java.util.ArrayList

class PairedDevicesAdapter(context: Context, activity: Activity): RecyclerView.Adapter<PairedDevicesAdapter.ViewHolder>() {

    var dataList: List<DevicesModel> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(private val binding: CellPairedDevicesBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: DevicesModel) {

            binding.deviceName.text = itemModel.deviceName
            binding.deviceAddress.text = itemModel.deviceAddress
            if (SettingActivity.isRadioChecked == 1) {
                binding.deviceIcon.setImageResource(R.drawable.adf_scanner)
            } else {
                binding.deviceIcon.setImageResource(R.drawable.print)
            }

            itemView.setOnClickListener {
                val paringDialog = PopupPairingDevice(mContext, mActivity)
                paringDialog.show(itemModel.deviceName, itemModel.deviceAddress)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PairedDevicesAdapter.ViewHolder {
        val binding = CellPairedDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: PairedDevicesAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}