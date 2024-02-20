package kr.co.kimberly.wma.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupProductPriceHistory
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderRegBinding
import kr.co.kimberly.wma.model.OrderRegModel
import java.util.ArrayList

class SlipInquiryModifyAdapter(mContext: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    var dataList: List<OrderRegModel> = ArrayList()
    var onItemClickedListener: OnItemClickedListener? = null
    var headerViewHolder: HeaderViewHolder? = null

    private var headerData: OrderRegModel? = null


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
            TYPE_HEADER -> {
                // HeaderViewHolder(HeaderRegBinding.inflate(inflater, parent, false))
                val viewHolder = HeaderViewHolder(HeaderRegBinding.inflate(inflater, parent, false))
                headerViewHolder = viewHolder
                viewHolder
            }
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false), onItemClickedListener!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(dataList[position - 1]) // 헤더가 있으므로 position - 1

                if (position == itemCount - 1) {
                    holder.binding.borderView.visibility = View.INVISIBLE // 숨김
                } else {
                    holder.binding.borderView.visibility = View.VISIBLE // 표시
                }
            }
            is HeaderViewHolder -> {
                holder.bind(headerData)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size + 1 // 헤더뷰를 포함
    }

    class ViewHolder(val binding: CellOrderRegBinding, val onItemClickedListener: OnItemClickedListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderRegModel) {
            binding.orderName.text = item.orderName

            // 데이터 바인딩
            itemView.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    onItemClickedListener.onItemClicked(item)
                }
            })
        }
    }

    class HeaderViewHolder(private val headerBinding: HeaderRegBinding) : RecyclerView.ViewHolder(headerBinding.root) {
        fun bind(item: OrderRegModel?) {
            if(item != null) {
                headerBinding.searchResult.text = item.orderName
            }
        }
    }

    fun headerUpdate(item: OrderRegModel) {
        headerData = item
    }

    interface OnItemClickedListener {
        fun onItemClicked(item: OrderRegModel)
    }
}