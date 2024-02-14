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
import kr.co.kimberly.wma.databinding.CellCollectListBinding
import kr.co.kimberly.wma.databinding.CellPairedDevicesListBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.DevicesModel
import java.util.ArrayList

class CollectListAdapter(context: Context, activity: Activity): RecyclerView.Adapter<CollectListAdapter.ViewHolder>() {

    var dataList: List<AccountModel> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(private val binding: CellCollectListBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: AccountModel) {

            binding.receiptNumber.text = "전표 : ${itemModel.receiptNumber}"
            binding.account.text = "거래처 : ${itemModel.account}"
            binding.amount.text = "금액: ${itemModel.amount}"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectListAdapter.ViewHolder {
        val binding = CellCollectListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: CollectListAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}