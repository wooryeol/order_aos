package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.TotalValueListener
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderPurchaseRequesetBinding
import kr.co.kimberly.wma.menu.purchase.PurchaseApprovalActivity
import kr.co.kimberly.wma.menu.purchase.PurchaseRequestActivity
import kr.co.kimberly.wma.model.OrderRegModel
import kr.co.kimberly.wma.model.SearchResultModel
import java.text.DecimalFormat
import java.util.ArrayList

class PurchaseRequestAdapter(mContext: Context, activity: Activity, private val updateData: ((ArrayList<OrderRegModel>, String, String) -> Unit)): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    var dataList: ArrayList<OrderRegModel> = ArrayList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        var totalValue: Int = 0
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
            TYPE_HEADER -> HeaderViewHolder(HeaderPurchaseRequesetBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(dataList[position - 1]) // 헤더가 있으므로 position - 1
                val data = dataList[position-1]

                holder.binding.deleteButton.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val popupDoubleMessage = PopupDoubleMessage(v.context, "제품 삭제", data.orderName, "선택한 제품이 주문리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                            override fun onCancelClick() {
                                Log.d("tttt", "취소 클릭함")
                            }

                            @SuppressLint("NotifyDataSetChanged")
                            override fun onOkClick() {
                                removeItem(data)
                            }
                        }
                        popupDoubleMessage.show()
                    }
                })

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
        @SuppressLint("SetTextI18n")
        fun bind(item: OrderRegModel) {
            // 데이터 바인딩
            // 예: binding.textView.text = data.someText

            binding.tvBox.text = item.box
            binding.tvEach.text = item.each
            binding.tvPrice.text = "${item.unitPrice}원"
            binding.tvTotal.text = item.totalQty
            binding.tvTotalAmount.text = "${item.totalAmount}원"
        }
    }

    inner class HeaderViewHolder(private val binding: HeaderPurchaseRequesetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val list = ArrayList<SearchResultModel>()

            binding.sapCode.isSelected = true
            binding.address.isSelected = true

            binding.accountArea.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    list.clear()
                    for(i: Int in 1..15) {
                        list.add(SearchResultModel("(30900785) (주) 명보유통 [$i]"))
                    }

                    val popupSearchResult = PopupSearchResult(binding.root.context, list)
                    popupSearchResult.onItemSelect = {
                        binding.sapCode.text = it.name
                        clear(it.name)
                    }
                    popupSearchResult.show()
                }
            })

            binding.addressArea.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    list.clear()
                    for(i: Int in 1..15) {
                        list.add(SearchResultModel("(09087754) 비즈위즈 시스템 [$i]"))
                    }

                    val popupSearchResult = PopupSearchResult(binding.root.context, list)
                    popupSearchResult.onItemSelect = {
                        binding.address.text = it.name
                        // PurchaseApprovalActivity.purchaseAddress = it.name
                        clear(it.name)
                    }
                    popupSearchResult.show()
                }
            })

            binding.btSearch.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    list.clear()
                    if (binding.etProductName.text.isNullOrEmpty()) {
                        Toast.makeText(v.context, "제품명을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        for (i: Int in 1..15) {
                            list.add(SearchResultModel("(38293) 하기스 프리미어 물티슈 60*3+1 [$i]"))
                        }
                        val popupSearchResult =
                            PopupSearchResult(binding.root.context, list)
                        popupSearchResult.onItemSelect = {
                            binding.searchResult.text = it.name
                            binding.etProductName.visibility = View.GONE
                            binding.tvProductName.visibility = View.VISIBLE
                            binding.tvProductName.isSelected = true
                            binding.tvProductName.text = it.name
                        }
                        popupSearchResult.show()
                    }
                }
            })

            binding.btAddOrder.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {

                    /*val popupProductPriceHistory = PopupProductPriceHistory(binding.root.context)
                     popupProductPriceHistory.show()*/

                    if (binding.etBox.text.isNullOrEmpty() || binding.etPrice.text.isNullOrEmpty() || binding.searchResult.text == "검색된 제품명") {
                        Toast.makeText(v.context, "모든 항목을 채워주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            val decimal = DecimalFormat("#,###")
                            val sapName = binding.sapCode.text.toString()
                            val address = binding.address.text.toString()
                            val box = binding.etBox.text.toString().toInt()
                            val unitPrice = binding.etPrice.text.toString().toInt()
                            val totalQty = 24 * box
                            val totalAmount = totalQty * unitPrice

                            if (box == 0 || unitPrice == 0) {
                                Toast.makeText(v.context, "박스와 단가에는 0이 들어갈 수 없습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                if (box == 0) {
                                    Toast.makeText(
                                        v.context,
                                        "박스 수량을 확인해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val model = OrderRegModel(
                                        list[list.size - 1].name,
                                        decimal.format(box).toString(),
                                        "0",
                                        decimal.format(unitPrice).toString(),
                                        decimal.format(totalQty).toString(),
                                        decimal.format(totalAmount).toString()
                                    )

                                    addItem(model, sapName, address)

                                    binding.etProductName.text = null
                                    binding.etProductName.visibility = View.VISIBLE
                                    binding.tvProductName.text = null
                                    binding.tvProductName.visibility = View.GONE
                                    binding.searchResult.text =
                                        v.context.getString(R.string.searchResult)
                                    binding.etBox.text = null
                                    binding.etPrice.text = null
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("test log", "e >>> $e")
                            Toast.makeText(v.context, "올바른 값을 입력해주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
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
                    binding.tvProductName.text = null
                    binding.tvProductName.visibility = View.GONE
                    binding.etProductName.visibility = View.VISIBLE
                    binding.etProductName.hint = v.context.getString(R.string.productNameHint)
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: OrderRegModel, accountName: String, address: String) {
        dataList.add(item)
        notifyDataSetChanged()
        updateData(dataList, accountName, address)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(item: OrderRegModel) {
        dataList.remove(item)
        notifyDataSetChanged()
        updateData(dataList, "", "")
    }

    fun clear(accountName: String) {
        dataList.clear()
        updateData(dataList, accountName, "")
    }
}