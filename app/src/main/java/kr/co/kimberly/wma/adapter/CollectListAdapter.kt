package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.combine
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupPairingDevice
import kr.co.kimberly.wma.databinding.CellCollectBinding
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.menu.slip.SlipInquiryDetailActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.DevicesModel
import java.util.ArrayList

class CollectListAdapter(context: Context, activity: Activity): RecyclerView.Adapter<CollectListAdapter.ViewHolder>() {
    var dataList: List<AccountModel> = ArrayList()
    var mContext = context
    var mActivity = activity
    var isSlipAct = false

    inner class ViewHolder(private val binding: CellCollectBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: AccountModel) {
            binding.receiptNumber.text = "전표 : ${itemModel.receiptNumber}"
            binding.account.text = "거래처 : ${itemModel.account}"
            binding.amount.text = "금액: ${itemModel.amount}"

            itemView.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if(isSlipAct) { // 전표조회에서 진입했을 경우
                        val intent = Intent(mContext, SlipInquiryDetailActivity::class.java)
                        mContext.startActivity(intent)
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectListAdapter.ViewHolder {
        val binding = CellCollectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: CollectListAdapter.ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}