package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupProductPriceHistory
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderRegBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import kr.co.kimberly.wma.network.model.ProductPriceHistoryModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import retrofit2.Call
import retrofit2.Response
import kotlin.math.ceil

class SlipInquiryModifyAdapter(mContext: Context,val customerCd: String, val customerNm: String, private val updateData: (ArrayList<SearchItemModel>) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    private var headerViewHolder: HeaderViewHolder? = null
    var slipList: ArrayList<SearchItemModel>? = null // 받아온 아이템 리스트
    var selectedItem: SearchItemModel? = null // 선택된 제품
    var historyList: List<ProductPriceHistoryModel>? = null // 제품 단가 이력 리스트
    var popupSearchResult : PopupSearchResult? = null // 아이템 리스트
    var popupProductPriceHistory : PopupProductPriceHistory? = null // 단가 이력 팝업
    var onItemSelect: ((SearchItemModel) -> Unit)? = null
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보

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
                val viewHolder = HeaderViewHolder(HeaderRegBinding.inflate(inflater, parent, false))
                headerViewHolder = viewHolder
                viewHolder
            }
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false))
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mLoginInfo = Utils.getLoginData()!!
        when (holder) {
            is ViewHolder -> {
                holder.bind(slipList!![position - 1]) // 헤더가 있으므로 position - 1

                val data = slipList!![position-1]


                holder.binding.deleteButton.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val popupDoubleMessage = PopupDoubleMessage(v.context, "제품 삭제", data.itemNm!!, "선택한 제품이 주문리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                            override fun onCancelClick() {
                                Utils.Log("취소 클릭함")
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

    inner class ViewHolder(val binding: CellOrderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(item: SearchItemModel) {
            itemView.setOnClickListener(object : OnSingleClickListener(){
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    onItemSelect?.invoke(item)
                    selectedItem = item
                    /*val popupProductPriceHistory = PopupProductPriceHistory(binding.root.context)
                    popupProductPriceHistory.show()

                    val date = Date()
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                    val formattedDate = dateFormat.format(date)
                    PopupProductPriceHistory.productPriceHistory.clear()
                    PopupProductPriceHistory.productPriceHistory.add(ProductPriceHistoryModel(formattedDate, "${binding.tvPrice.text}"))*/

                    //editList = item!!
                    //notifyDataSetChanged()
                }
            })

            binding.orderName.text = item.itemNm
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

            headerBinding.accountName.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    Utils.popupNotice(context, "거래처를 변경할 수 없습니다.")
                }

            })

            // 제품 수정
            onItemSelect = {
                clearButton()
                headerBinding.btAddOrder.text = context.getString(R.string.editOrder)
                headerBinding.searchResult.text = it.itemNm
                headerBinding.etBox.setText(it.boxQty.toString())
                headerBinding.etEach.setText(it.unitQty.toString())
                headerBinding.etPrice.setText(it.netPrice.toString())
            }

            // 제품 검색
            headerBinding.btSearch.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("SetTextI18n")
                override fun onSingleClick(v: View) {
                    if (headerBinding.accountName.text == context.getString(R.string.accountHint)) {
                        PopupNotice(context,"거래처를 먼저 검색해주세요").show()
                    } else {
                        if (headerBinding.etProductName.text.isNullOrEmpty()) {
                            Utils.popupNotice(v.context, "제품명을 입력해주세요")
                        } else {
                            // 아이템 리스트 검색
                            searchItem(headerBinding.etProductName.text.toString(), headerBinding.root.context)
                        }
                    }
                }
            })

            // 제품 가격 조회
            headerBinding.searchResult.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (headerBinding.searchResult.text != context.getString(R.string.searchResult)) {
                        searchItemPriceHistory()
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

            /*headerBinding.etProductName.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    clearButton()
                }
            })*/

            headerBinding.etProductName.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    headerBinding.btSearch.performClick()
                    true
                } else {
                    false
                }
            }

            headerBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    clearButton()
                }
            })

            headerBinding.etPrice.setOnEditorActionListener{_, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    headerBinding.btAddOrder.performClick()
                    true
                } else {
                    false
                }
            }

            // 아이템 등록
            headerBinding.btAddOrder.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    if (headerBinding.etPrice.text.isNullOrEmpty() || headerBinding.searchResult.text == context.getString(R.string.searchResult)) {
                        Utils.popupNotice(v.context, "모든 항목을 채워주세요")
                    } else {
                        try {
                            if (headerBinding.etBox.text.isNullOrEmpty()) {
                                headerBinding.etBox.setText("0")
                            }
                            if (headerBinding.etEach.text.isNullOrEmpty()) {
                                headerBinding.etEach.setText("0")
                            }
                            val itemName = headerBinding.searchResult.text.toString()
                            val itemCd = selectedItem?.itemCd
                            val boxQty = Utils.getIntValue(headerBinding.etBox.text.toString())
                            val unitQty = Utils.getIntValue(headerBinding.etEach.text.toString())
                            val netPrice = Utils.getIntValue(headerBinding.etPrice.text.toString())
                            val saleQty = selectedItem?.getBox!! * boxQty + unitQty
                            val amount = saleQty * netPrice
                            val supplyPrice = if (selectedItem?.vatYn == "01") {
                                ceil(amount / 1.1).toInt()
                            } else {
                                amount
                            }
                            val vat = amount - supplyPrice
                            if (netPrice == 0) {
                                Utils.popupNotice(context, "단가에는 0이 들어갈 수 없습니다.")
                            } else {
                                if (boxQty == 0 && unitQty == 0) {
                                    Utils.popupNotice(context, "박스 혹은 낱개의 수량을 확인해주세요")
                                } else {
                                    val model = SearchItemModel(
                                        itemNm = itemName,
                                        itemCd = itemCd!!,
                                        netPrice = netPrice,
                                        getBox = selectedItem?.getBox!!,
                                        boxQty = boxQty,
                                        unitQty = unitQty,
                                        saleQty = saleQty,
                                        supplyPrice = supplyPrice,
                                        vat = vat,
                                        amount = amount
                                    )

                                    Utils.Log("added item =====> ${Gson().toJson(model)}")
                                    addItem(model)

                                    headerBinding.etProductName.text = null
                                    headerBinding.etProductName.visibility = View.VISIBLE
                                    headerBinding.tvProductName.text = null
                                    headerBinding.tvProductName.visibility = View.GONE
                                    headerBinding.searchResult.text = v.context.getString(R.string.searchResult)
                                    headerBinding.etBox.setText(v.context.getString(R.string.zero))
                                    headerBinding.etEach.setText(v.context.getString(R.string.zero))
                                    headerBinding.etPrice.setText(v.context.getString(R.string.zero))
                                }
                            }
                        } catch (e: Exception) {
                            Utils.Log("error >>> ${Log.getStackTraceString(e)}")
                            Utils.popupNotice(v.context, "올바른 값을 입력해주세요")
                        }

                    }
                    headerBinding.btAddOrder.text = context.getString(R.string.addOrder)
                }
            })
        }

        // 단가 정보 조회
        fun searchItemPriceHistory(){
            val service = ApiClientService.retrofit.create(ApiClientService::class.java)
            val call = service.history(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, customerCd!!, selectedItem!!.itemCd!!)

            call.enqueue(object : retrofit2.Callback<ListResultModel<ProductPriceHistoryModel>> {
                override fun onResponse(
                    call: Call<ListResultModel<ProductPriceHistoryModel>>,
                    response: Response<ListResultModel<ProductPriceHistoryModel>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                            Utils.Log("price history search success ====> ${Gson().toJson(item)}")
                            historyList = item.data
                            popupProductPriceHistory = PopupProductPriceHistory(context, historyList!!, selectedItem!!.itemNm!!)
                            popupProductPriceHistory?.show()
                        } else {
                            Utils.popupNotice(context, context.getString(R.string.error))
                        }
                    } else {
                        Utils.Log("${response.code()} ====> ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ListResultModel<ProductPriceHistoryModel>>, t: Throwable) {
                    Utils.Log("item search failed ====> ${t.message}")
                }

            })
        }

        // 검색 아이템 리스트 조회
        fun searchItem(searchCondition: String, context: Context) {
            val service = ApiClientService.retrofit.create(ApiClientService::class.java)
            val searchType = Define.SEARCH
            val orderYn = Define.PURCHASE_NO

            val call = service.item(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, customerCd!!, searchType, orderYn, searchCondition)

            call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<SearchItemModel>>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<ObjectResultModel<DataModel<SearchItemModel>>>,
                    response: Response<ObjectResultModel<DataModel<SearchItemModel>>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                            //Utils.Log("item search success ====> ${Gson().toJson(item.data?.firstOrNull()?.itemList)}")
                            Utils.Log("item search success ====> ${Gson().toJson(item.data)}")
                            if (item.data?.itemList.isNullOrEmpty()) {
                                Utils.popupNotice(context, context.getString(R.string.error))
                            } else {
                                val itemList = item.data?.itemList!!
                                popupSearchResult = PopupSearchResult(context, itemList)
                                popupSearchResult?.show()

                                // 팝업 선택 시
                                popupSearchResult?.onItemSelect = {
                                    slipList?.forEach { data ->
                                        clearButton()
                                        if (data.itemCd == it.itemCd ) {
                                            PopupNotice(context, "이미 등록된 제품입니다.").show()
                                        } else {
                                            headerBinding.searchResult.text = "(${it.itemCd}) ${it.itemNm}"
                                            headerBinding.etProductName.visibility = View.GONE
                                            headerBinding.btProductNameEmpty.visibility = View.VISIBLE
                                            headerBinding.tvProductName.visibility = View.VISIBLE
                                            headerBinding.tvProductName.isSelected = true
                                            headerBinding.tvProductName.text = "(${it.itemCd}) ${it.itemNm}"
                                            selectedItem = SearchItemModel(
                                                it.itemCd,
                                                it.itemNm,
                                                it.whStock,
                                                it.getBox,
                                                it.vatYn,
                                                it.netPrice
                                            )
                                            Utils.Log("RegAdapter selected item ====> ${Gson().toJson(selectedItem)}")
                                        }
                                    }
                                }
                            }
                        } else {
                            PopupNotice(context, item?.returnMsg!!).show()
                            Utils.Log("returnMsg ====> ${item.returnMsg}")
                        }
                    } else {
                        Utils.Log("${response.code()} ====> ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ObjectResultModel<DataModel<SearchItemModel>>>, t: Throwable) {
                    Utils.Log("item search failed ====> ${t.message}")
                }

            })
        }

        fun clearButton() {
            headerBinding.etProductName.text = null
            headerBinding.searchResult.text = context.getString(R.string.searchResult)
            headerBinding.tvProductName.text = null
            headerBinding.tvProductName.visibility = View.GONE
            headerBinding.etProductName.visibility = View.VISIBLE
            headerBinding.etProductName.hint = context.getString(R.string.productNameHint)
            headerBinding.etBox.setText("0")
            headerBinding.etEach.setText("0")
            headerBinding.etPrice.setText("0")
        }
    }

    interface OnItemClickedListener {
        fun onItemClicked(item: SearchItemModel)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: SearchItemModel) {
        slipList!!.removeAll { it.itemCd == item.itemCd }
        slipList!!.add(item)
        notifyDataSetChanged()
        Utils.Log("updated slipList ====> ${Gson().toJson(slipList)}")
        updateData(slipList!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(item: SearchItemModel) {
        slipList!!.remove(item)
        notifyDataSetChanged()
        Utils.Log("updated slipList ====> ${Gson().toJson(slipList)}")
        updateData(slipList!!)
    }
}