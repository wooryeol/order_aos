package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.TotalValueListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.model.OrderRegModel

class SlipInquiryDetailAdapter(context: Context, private val updateData: ((ArrayList<OrderRegModel>, String) -> Unit)): RecyclerView.Adapter<SlipInquiryDetailAdapter.ViewHolder>() {
    var dataList: ArrayList<OrderRegModel> = ArrayList()
    var mContext = context

    inner class ViewHolder(val binding: CellOrderRegBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: OrderRegModel) {

            binding.orderName.text = itemModel.orderName
            binding.tvBox.text = itemModel.box
            binding.tvEach.text = itemModel.each
            binding.tvPrice.text = "${itemModel.unitPrice}원"
            binding.tvTotal.text = itemModel.totalQty
            binding.tvTotalAmount.text = "${itemModel.totalAmount}원"
            binding.deleteButton.visibility = View.GONE

            itemView.setOnClickListener {
                /*val intent = Intent(itemView.context, MessageActivity::class.java)
                intent.putExtra(Define.UNIQUE, itemModel.name)
                intent.putExtra(Define.D_COUNT, binding.dCount.text.toString())
                intent.putExtra(Define.MEMBER_TYPE, itemModel.type)
                intent.putExtra(Define.MAIN_COLOR, itemModel.color)
                intent.putExtra(Define.MAIN_NAME, itemModel.name_kor)
                intent.putExtra(Define.TOP_THUMB, itemModel.top_thumbnail)
                intent.putExtra(Define.TOP_THUMB_LINK, itemModel.top_thumbnail_link)
                intent.putExtra(Define.CHANGE_THUMB, itemModel.change_thumb)
                intent.putExtra(Define.MENU_SW, isMenuSw)
                itemView.context.startActivity(intent)
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)*/
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CellOrderRegBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])

        if(position == (itemCount - 1)) {
            holder.binding.borderView.visibility = View.INVISIBLE
        }
    }
}