package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.awaitAll
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.OrderRegModel
import java.util.ArrayList

class PairedDevicesAdapter(context: Context, activity: Activity): RecyclerView.Adapter<PairedDevicesAdapter.ViewHolder>() {
    var dataList: List<BluetoothDevice> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(private val binding: CellPairedDevicesBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(itemModel: BluetoothDevice) {

            binding.deviceName.text = itemModel.name
            binding.deviceAddress.text = itemModel.address

            // 기기에 따라 보여주는 아이콘 다르게l
            if (itemModel.name.startsWith("Alpha") || SettingActivity.isRadioChecked == 2) {
                binding.deviceIcon.setImageResource(R.drawable.print)
            } else {
                binding.deviceIcon.setImageResource(R.drawable.adf_scanner)
            }

            // 페어링 안되었을 때만 클릭이 가능하도록
            if (itemModel.bondState == 10) {
                itemView.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val paringDialog = PopupPairingDevice(mContext, mActivity)
                        paringDialog.show(itemModel)
                    }
                })
            } else if(itemModel.bondState == 12) {
                itemView.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {

                    }
                })
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