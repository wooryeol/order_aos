package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupProductPriceHistory
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderRegBinding
import kr.co.kimberly.wma.menu.slip.SlipInquiryDetailActivity
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.OrderRegModel
import kr.co.kimberly.wma.network.model.SalesInfoModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import kr.co.kimberly.wma.network.model.SearchResultModel

class SlipInquiryModifyAdapter(mContext: Context,val customerCd: String, val customerNm: String, private val updateData: (ArrayList<SearchItemModel>) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    var slipList: ArrayList<SearchItemModel>? = null // 받아온 아이템 리스트
    var onItemClickedListener: OnItemClickedListener? = null
    var headerViewHolder: HeaderViewHolder? = null
    var editList: SearchItemModel? = null
    var onItemSelect: ((SalesInfoModel) -> Unit)? = null
    private var headerData: SearchItemModel? = null

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
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(slipList!![position - 1]) // 헤더가 있으므로 position - 1

                val data = slipList!![position-1]


                holder.binding.deleteButton.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val popupDoubleMessage = PopupDoubleMessage(v.context, "제품 삭제", data.itemNm!!, "선택한 제품이 주문리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                            override fun onCancelClick() {
                                Log.d("tttt", "취소 클릭함")
                            }

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
        return slipList!!.size + 1 // 헤더뷰를 포함
    }

    inner class ViewHolder(val binding: CellOrderRegBinding, val onItemClickedListener: OnItemClickedListener) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(item: SearchItemModel) {

            itemView.setOnClickListener(object : OnSingleClickListener(){
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    //onItemSelect?.invoke(item)
                    /*val popupProductPriceHistory = PopupProductPriceHistory(binding.root.context)
                    popupProductPriceHistory.show()

                    val date = Date()
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                    val formattedDate = dateFormat.format(date)
                    PopupProductPriceHistory.productPriceHistory.clear()
                    PopupProductPriceHistory.productPriceHistory.add(ProductPriceHistoryModel(formattedDate, "${binding.tvPrice.text}"))*/

                    editList = item!!
                    notifyDataSetChanged()
                }
            })

            binding.orderName.text = "(${item.itemCd}) ${item.itemNm}"
            binding.tvBoxEach.text = "BOX(${item.getBox}EA): "
            binding.tvBox.text = Utils.decimal(item.boxQty!!)
            binding.tvEach.text = Utils.decimal(item.unitQty!!)
            binding.tvPrice.text = "${Utils.decimal(item.netPrice!!)}원"
            binding.tvTotal.text = Utils.decimal(item.saleQty!!)
            binding.tvTotalAmount.text = "${Utils.decimal(item.amount!!)}원"
        }
    }

    inner class HeaderViewHolder(private val headerBinding: HeaderRegBinding) : RecyclerView.ViewHolder(headerBinding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {

            headerBinding.accountName.text = "($customerCd) $customerNm"

            if (editList != null) {
                /*headerBinding.searchResult.text = editList?.orderName
                headerBinding.etBox.setText((editList?.box)?.replace(",", ""))
                headerBinding.etEach.setText((editList?.each?.replace(",", "")))
                headerBinding.etPrice.setText((editList?.unitPrice)?.replace(",", ""))
                headerBinding.btAddOrder.setText(R.string.productModify)*/

                headerBinding.btAddOrder.setOnClickListener(object: OnSingleClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onSingleClick(v: View) {

                        val accountName = headerBinding.accountName.text.toString()
                        val box = headerBinding.etBox.text.toString().toInt()
                        val each = headerBinding.etEach.text.toString().toInt()
                        val unitPrice = headerBinding.etPrice.text.toString().toInt()
                        val totalQty = 24 * box + each
                        val totalAmount = totalQty * unitPrice

                        /*val model = OrderRegModel(
                            accountName,
                            headerBinding.searchResult.text.toString(),
                            decimal.format(box).toString(),
                            decimal.format(each).toString(),
                            decimal.format(unitPrice).toString(),
                            decimal.format(totalQty).toString(),
                            decimal.format(totalAmount).toString()
                        )

                        dataList.removeIf{it.orderName == model.orderName}

                        addItem(model, accountName)*/

                        headerBinding.etPrice.text = null
                        headerBinding.etEach.text = null
                        headerBinding.etBox.text = null
                        headerBinding.btAddOrder.text = v.context.getString(R.string.addOrder)
                        headerBinding.searchResult.text = v.context.getString(R.string.searchResult)
                        notifyDataSetChanged()
                    }

                })
                editList = null
            } else {

                //headerBinding.accountName.text = SlipInquiryDetailActivity.accountName

                val list = ArrayList<SearchResultModel>()

                headerBinding.btSearch.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        if (headerBinding.etProductName.text.isNullOrEmpty()) {
                            Toast.makeText(v.context, "제품명을 입력해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            /*val popupSearchResult = PopupSearchResult(headerBinding.root.context, list)
                            popupSearchResult.onItemSelect = {
                                headerBinding.searchResult.text = it.name
                                headerBinding.etProductName.visibility = View.GONE
                                headerBinding.tvProductName.visibility = View.VISIBLE
                                headerBinding.tvProductName.isSelected = true
                                headerBinding.tvProductName.text = it.name
                                headerBinding.btAddOrder.setText(R.string.addOrder)
                                headerBinding.etBox.text = null
                                headerBinding.etEach.text = null
                                headerBinding.etPrice.text = null
                            }
                            popupSearchResult.show()*/
                        }
                    }
                })


                headerBinding.btAddOrder.setOnClickListener(object : OnSingleClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onSingleClick(v: View) {
                        if (headerBinding.etPrice.text.isNullOrEmpty() || headerBinding.searchResult.text == context.getString(
                                R.string.searchResult
                            )
                        ) {
                            Toast.makeText(v.context, "모든 항목을 채워주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                if (headerBinding.etBox.text.isNullOrEmpty()) {
                                    headerBinding.etBox.setText("0")
                                }

                                if (headerBinding.etEach.text.isNullOrEmpty()) {
                                    headerBinding.etEach.setText("0")
                                }

                                val accountName = headerBinding.accountName.text.toString()
                                val box = headerBinding.etBox.text.toString().toInt()
                                val each = headerBinding.etEach.text.toString().toInt()
                                val unitPrice = headerBinding.etPrice.text.toString().toInt()
                                val totalQty = 24 * box + each
                                val totalAmount = totalQty * unitPrice

                                if (unitPrice == 0) {
                                    Toast.makeText(
                                        context,
                                        "단가에는 0이 들어갈 수 없습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    if (box == 0 && each == 0) {
                                        Toast.makeText(
                                            context,
                                            "박스 혹은 낱개의 수량을 확인해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        /*val model = OrderRegModel(
                                            accountName,
                                            list[list.size - 1].name,
                                            decimal.format(box).toString(),
                                            decimal.format(each).toString(),
                                            decimal.format(unitPrice).toString(),
                                            decimal.format(totalQty).toString(),
                                            decimal.format(totalAmount).toString()
                                        )

                                        addItem(model, accountName)*/

                                        headerBinding.etProductName.text = null
                                        headerBinding.etProductName.visibility = View.VISIBLE
                                        headerBinding.tvProductName.text = null
                                        headerBinding.tvProductName.visibility = View.GONE
                                        headerBinding.searchResult.text =
                                            v.context.getString(R.string.searchResult)
                                        headerBinding.etBox.text = null
                                        headerBinding.etEach.text = null
                                        headerBinding.etPrice.text = null
                                    }
                                }
                            } catch (e: Exception) {
                                Utils.Log("error ====> $e")
                                Toast.makeText(v.context, "올바른 값을 입력해주세요", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                })

                headerBinding.etProductName.addTextChangedListener {
                    if (headerBinding.etProductName.text.isNullOrEmpty()) {
                        headerBinding.btProductNameEmpty.visibility = View.GONE
                    } else {
                        headerBinding.btProductNameEmpty.visibility = View.VISIBLE
                    }
                }

                headerBinding.btProductNameEmpty.setOnClickListener(object :
                    OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        headerBinding.etProductName.text = null
                        headerBinding.searchResult.text = v.context.getString(R.string.searchResult)
                        headerBinding.tvProductName.text = null
                        headerBinding.tvProductName.visibility = View.GONE
                        headerBinding.etProductName.visibility = View.VISIBLE
                        headerBinding.etProductName.hint =
                            v.context.getString(R.string.productNameHint)
                    }
                })
            }
        }
    }

    fun headerUpdate(item: SearchItemModel) {
        headerData = item
    }

    interface OnItemClickedListener {
        fun onItemClicked(item: SearchItemModel)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: SearchItemModel) {
        slipList!!.add(item)
        notifyDataSetChanged()
        updateData(slipList!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(item: SearchItemModel) {
        slipList!!.remove(item)
        notifyDataSetChanged()
        updateData(slipList!!)
    }
}