package kr.co.kimberly.wma.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.databinding.CellMeinMenuBinding
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderOrderRegBinding
import kr.co.kimberly.wma.databinding.LayoutHeaderBinding
import kr.co.kimberly.wma.model.MainMenuModel
import kr.co.kimberly.wma.model.OrderRegModel
import java.util.ArrayList

class OrderRegAdapter(context: Context, activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: List<OrderRegModel> = ArrayList()

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
            TYPE_HEADER -> HeaderViewHolder(HeaderOrderRegBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false))
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
        }
    }

    override fun getItemCount(): Int {
        return dataList.size + 1 // 헤더뷰를 포함
    }

    class ViewHolder(val binding: CellOrderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderRegModel) {
            // 데이터 바인딩
            // 예: binding.textView.text = data.someText
        }
    }

    class HeaderViewHolder(binding: HeaderOrderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        // 헤더 뷰 초기화
    }
}