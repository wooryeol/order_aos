package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.combine
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellCollectBinding
import kr.co.kimberly.wma.databinding.CellInventoryBinding
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.DevicesModel
import kr.co.kimberly.wma.model.InventoryModel
import java.util.ArrayList

class InventoryListAdapter(context: Context, activity: Activity): RecyclerView.Adapter<InventoryListAdapter.ViewHolder>() {

    var dataList: List<InventoryModel> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(private val binding: CellInventoryBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: InventoryModel) {

            binding.productName.text = itemModel.productName
            binding.box.text = itemModel.box
            binding.each.text = itemModel.each
            binding.total.text = itemModel.total
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InventoryListAdapter.ViewHolder {
        val binding = CellInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: InventoryListAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}