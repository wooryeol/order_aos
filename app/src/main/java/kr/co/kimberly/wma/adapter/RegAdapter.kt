package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.combine
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupProductPriceHistory
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderRegBinding
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.model.OrderRegModel
import kr.co.kimberly.wma.model.SearchResultModel
import java.util.ArrayList

class RegAdapter(mContext: Context, activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
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
            TYPE_HEADER -> HeaderViewHolder(HeaderRegBinding.inflate(inflater, parent, false))
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
            is HeaderViewHolder -> {
                holder.bind()
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
            itemView.setOnClickListener {
                val popupProductPriceHistory = PopupProductPriceHistory(binding.root.context)
                popupProductPriceHistory.show()
            }

            binding.tvBox.text = item.box
            binding.tvEach.text = item.each
            binding.tvPrice.text = item.unitPrice
            binding.tvTotal.text = item.totalQty
            binding.tvTotalAmount.text = item.totalAmount

            binding.deleteButton.setOnClickListener {

            }
        }
    }

    class HeaderViewHolder(private val binding: HeaderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val scanResultList = ArrayList<SearchResultModel>()

            binding.accountArea.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    val popupAccountSearch = PopupAccountSearch(binding.root.context)
                    popupAccountSearch.onItemSelect = {
                        binding.accountName.text = it.name
                    }
                    popupAccountSearch.show()
                }
            })

            binding.btSearch.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {

                    if (binding.etProductName.text.isNullOrEmpty()) {
                        Toast.makeText(v.context, "제품명을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        for(i: Int in 1..15) {
                            scanResultList.add(SearchResultModel("(38293) 하기스 프리미어 물티슈 60*3+1 [$i]"))
                        }

                        val popupSearchResult = PopupSearchResult(binding.root.context, scanResultList)
                        popupSearchResult.onItemSelect = {
                            binding.searchResult.text = it.name
                        }
                        popupSearchResult.show()
                    }
                }
            })


            binding.btAddOrder.setOnClickListener(object: OnSingleClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    val box = binding.etBox.text.toString()
                    val each = binding.etEach.text.toString()
                    val unitPrice = binding.etPrice.text.toString()
                    val totalQty = 24 * box.toInt() + each.toInt()
                    val totalAmount = totalQty * unitPrice.toInt()

                    OrderRegActivity.list.add(OrderRegModel(scanResultList[0].name, box, each, unitPrice, totalQty.toString(), totalAmount.toString()))
                    OrderRegActivity.adapter.notifyDataSetChanged()
                }
            })

            binding.etProductName.addTextChangedListener {
                if (binding.etProductName.text.isNullOrEmpty()) {
                    binding.btProductNameEmpty.visibility = View.GONE
                } else {
                    binding.btProductNameEmpty.visibility = View.VISIBLE
                }
            }

            binding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    binding.etProductName.text = null
                    binding.searchResult.text = v.context.getString(R.string.searchResult)
                }
            })


        }
    }
}