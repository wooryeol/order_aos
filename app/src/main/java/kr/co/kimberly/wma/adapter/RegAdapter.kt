package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
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
import kr.co.kimberly.wma.network.model.SalesInfoModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import retrofit2.Call
import retrofit2.Response
import kotlin.math.ceil

class RegAdapter(mContext: Context, mActivity: Activity, private val updateData: ((ArrayList<SalesInfoModel>, String) -> Unit)): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    var activity = mActivity
    var dataList: ArrayList<SalesInfoModel> = ArrayList()
    var selectedItem: SearchItemModel? = null // 선택된 제품을 담는 리스트
    var historyList: List<ProductPriceHistoryModel>? = null // 제품 단가 이력 리스트
    var popupSearchResult : PopupSearchResult? = null // 아이템 리스트
    var popupProductPriceHistory : PopupProductPriceHistory? = null // 단가 이력 팝업
    var popupResultNothing : PopupNotice? = null // 조회 내역 없을 때
    var onItemSelect: ((SalesInfoModel) -> Unit)? = null
    var customerCd: String ? = null

    private var accountName : String? = null
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
        mLoginInfo = Utils.getLoginData()
        popupResultNothing = PopupNotice(context, context.getString(R.string.error))
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(HeaderRegBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(dataList[position - 1]) // 헤더가 있으므로 position - 1
                val data = dataList[position-1]

                holder.binding.deleteButton.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val popupDoubleMessage = PopupDoubleMessage(v.context, "제품 삭제", data.itemNm!!, "선택한 제품이 주문리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                            override fun onCancelClick() {
                                Utils.Log("아이템 삭제 취소")
                            }

                            override fun onOkClick() {
                                removeItem(data)
                                Utils.Log("아이템 삭제")
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

    inner class ViewHolder(val binding: CellOrderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(item: SalesInfoModel) {
            // 데이터 바인딩
            // 예: binding.textView.text = data.someText
            itemView.setOnClickListener {
                onItemSelect?.invoke(item)

                /*val popupProductPriceHistory = PopupProductPriceHistory(binding.root.context)
                popupProductPriceHistory.show()
                val date = Date()
                val dateFormat = SimpleDateFormat("yyyy/MM/dd")
                val formattedDate = dateFormat.format(date)
                PopupProductPriceHistory.productPriceHistory.clear()
                PopupProductPriceHistory.productPriceHistory.add(ProductPriceHistoryModel(formattedDate, "${binding.tvPrice.text}"))
                editItem(item, accountName!!)*/
            }
            binding.orderName.text = item.itemNm
            binding.tvBoxEach.text = "BOX(${item.getBox}EA): "
            binding.tvBox.text = Utils.decimal(item.boxQty!!)
            binding.tvEach.text = Utils.decimal(item.unitQty!!)
            binding.tvPrice.text = "${Utils.decimal(item.netPrice!!)}원"
            binding.tvTotal.text = Utils.decimal(item.saleQty!!)
            binding.tvTotalAmount.text = "${Utils.decimal(item.amount!!)}원"
        }
    }

    inner class HeaderViewHolder(val binding: HeaderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind() {
            binding.accountArea.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    val popupAccountSearch = PopupAccountSearch(binding.root.context)
                    popupAccountSearch.onItemSelect = {
                        accountName = "(${it.custCd}) ${it.custNm} [${it.remainAmt}원]"
                        binding.accountName.text = accountName
                        customerCd = it.custCd
                    }
                    if (dataList.isNotEmpty() || !binding.tvProductName.text.isNullOrEmpty()) {
                        val popupNoticeV2 = PopupNoticeV2(v.context, "기존 주문이 완료되지 않았습니다.\n새로운 거래처를 검색하시겠습니까?",
                            object : Handler(Looper.getMainLooper()) {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun handleMessage(msg: Message) {
                                    when(msg.what) {
                                        Define.OK -> {
                                            binding.accountName.text = null
                                            binding.etProductName.text = null
                                            binding.searchResult.text = v.context.getString(R.string.searchResult)
                                            binding.tvProductName.text = null
                                            binding.tvProductName.visibility = View.GONE
                                            binding.etProductName.visibility = View.VISIBLE
                                            binding.etProductName.hint = v.context.getString(R.string.productNameHint)
                                            clear("")
                                            notifyDataSetChanged()
                                            popupAccountSearch.show()
                                        }
                                    }
                                }
                            })
                        popupNoticeV2.show()
                    } else {
                        popupAccountSearch.show()
                    }
                }
            })

            binding.btSearch.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("SetTextI18n")
                override fun onSingleClick(v: View) {
                    if (binding.accountName.text == context.getString(R.string.accountHint)) {
                        PopupNotice(context,"거래처를 먼저 검색해주세요").show()
                    } else {
                        if (binding.etProductName.text.isNullOrEmpty()) {
                            Toast.makeText(v.context, "제품명을 입력해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            // 아이템 리스트 검색
                            searchItem(binding.etProductName.text.toString(), binding.root.context)

                            // 팝업 선택 시
                            /*popupSearchResult?.onItemSelect = {
                                binding.searchResult.text = "(${it.itemCd}) ${it.itemNm}"
                                binding.etProductName.visibility = View.GONE
                                binding.tvProductName.visibility = View.VISIBLE
                                binding.tvProductName.isSelected = true
                                binding.tvProductName.text = "(${it.itemCd}) ${it.itemNm}"
                                selectedItem = SearchItemModel(
                                    it.itemCd,
                                    it.itemNm,
                                    it.aliasNm,
                                    it.whStock,
                                    it.getBox,
                                    it.vatYn,
                                    it.netPrice
                                )
                            }*/
                        }
                    }
                }
            })

            // 제품 가격 조회
            binding.searchResult.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (binding.searchResult.text != context.getString(R.string.searchResult)) {
                        searchItemPriceHistory()
                    }
                }
            })

            //제품 수정
            onItemSelect ={
                binding.btAddOrder.text = context.getString(R.string.editOrder)
                binding.searchResult.text = it.itemNm
                binding.etBox.setText(it.boxQty.toString())
                binding.etEach.setText(it.unitQty.toString())
                binding.etPrice.setText(it.netPrice.toString())
            }

            binding.btAddOrder.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    if (binding.etPrice.text.isNullOrEmpty() || binding.searchResult.text == context.getString(R.string.searchResult)) {
                        Toast.makeText(v.context, "모든 항목을 채워주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            if (binding.etBox.text.isNullOrEmpty()) {
                                binding.etBox.setText("0")
                            }

                            if (binding.etEach.text.isNullOrEmpty()) {
                                binding.etEach.setText("0")
                            }

                            val itemName = binding.searchResult.text.toString()
                            val itemCd = selectedItem?.itemCd
                            val boxQty = Utils.getIntValue(binding.etBox.text.toString())
                            val unitQty = Utils.getIntValue(binding.etEach.text.toString())
                            val netPrice = Utils.getIntValue(binding.etPrice.text.toString())
                            val saleQty = selectedItem?.getBox!! * boxQty + unitQty
                            val amount = saleQty * netPrice
                            val supplyPrice = if (selectedItem?.vatYn == "01") {
                                ceil(amount/1.1).toInt()
                            } else {
                                amount
                            }
                            val vat = amount - supplyPrice

                            if (netPrice == 0) {
                                Toast.makeText(context, "단가에는 0이 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                if (boxQty == 0 && unitQty == 0) {
                                    Toast.makeText(context, "박스 혹은 낱개의 수량을 확인해주세요", Toast.LENGTH_SHORT).show()
                                } else {
                                    val model = SalesInfoModel(
                                        itemNm = itemName,
                                        itemCd = itemCd!!,
                                        netPrice = netPrice,
                                        getBox = selectedItem?.getBox!!.toString(),
                                        boxQty = boxQty,
                                        unitQty = unitQty,
                                        saleQty = saleQty,
                                        supplyPrice = supplyPrice,
                                        vat = vat,
                                        amount = amount
                                    )

                                    Utils.Log("added item =====> ${Gson().toJson(model)}")
                                    addItem(model, accountName!!)

                                    binding.etProductName.text = null
                                    binding.etProductName.visibility = View.VISIBLE
                                    binding.tvProductName.text = null
                                    binding.tvProductName.visibility = View.GONE
                                    binding.searchResult.text = v.context.getString(R.string.searchResult)
                                    binding.etBox.setText(v.context.getString(R.string.zero))
                                    binding.etEach.setText(v.context.getString(R.string.zero))
                                    binding.etPrice.setText(v.context.getString(R.string.zero))
                                }
                            }
                        } catch (e: Exception) {
                            Utils.Log("error >>> $e")
                            Toast.makeText(v.context, "올바른 값을 입력해주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
                    binding.btAddOrder.text = context.getString(R.string.addOrder)
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
                        if (item?.returnMsg == Define.SUCCESS) {
                            Utils.Log("price history search success ====> ${Gson().toJson(item)}")
                            historyList = item.data
                            popupProductPriceHistory = PopupProductPriceHistory(context, historyList!!, selectedItem!!.itemNm!!)
                            popupProductPriceHistory?.show()
                        } else {
                            popupResultNothing?.show()
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
                override fun onResponse(
                    call: Call<ObjectResultModel<DataModel<SearchItemModel>>>,
                    response: Response<ObjectResultModel<DataModel<SearchItemModel>>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        if (item?.returnMsg == Define.SUCCESS) {
                            //Utils.Log("item search success ====> ${Gson().toJson(item.data?.firstOrNull()?.itemList)}")
                            Utils.Log("item search success ====> ${Gson().toJson(item.data)}")
                            if (item.data?.itemList.isNullOrEmpty()) {
                                popupResultNothing?.show()
                            } else {
                                val itemList = item.data?.itemList!!
                                popupSearchResult = PopupSearchResult(context, itemList)
                                popupSearchResult?.show()

                                // 팝업 선택 시
                                popupSearchResult?.onItemSelect = {
                                    binding.searchResult.text = "(${it.itemCd}) ${it.itemNm}"
                                    binding.etProductName.visibility = View.GONE
                                    binding.tvProductName.visibility = View.VISIBLE
                                    binding.tvProductName.isSelected = true
                                    binding.tvProductName.text = "(${it.itemCd}) ${it.itemNm}"
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
                        } else {
                            PopupNotice(context, item?.returnMsg!!)
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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: SalesInfoModel, accountName: String) {
        dataList.removeAll{ it.itemCd == item.itemCd}
        dataList.add(item)
        Utils.Log("updateData dataList ====> ${Gson().toJson(dataList)}")
        notifyDataSetChanged()
        updateData(dataList, accountName)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(item: SalesInfoModel) {
        dataList.remove(item)
        notifyDataSetChanged()
        updateData(dataList, "")
    }

    fun clear(itemName: String) {
        dataList.clear()
        updateData(dataList, itemName)
    }
}