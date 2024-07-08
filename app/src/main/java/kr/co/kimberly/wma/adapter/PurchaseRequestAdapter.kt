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
import kr.co.kimberly.wma.custom.popup.PopupOk
import kr.co.kimberly.wma.custom.popup.PopupProductPriceHistory
import kr.co.kimberly.wma.custom.popup.PopupSAP
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.CellOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderPurchaseRequesetBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import kr.co.kimberly.wma.network.model.ProductPriceHistoryModel
import kr.co.kimberly.wma.network.model.SalesInfoModel
import kr.co.kimberly.wma.network.model.SapModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import retrofit2.Call
import retrofit2.Response

class PurchaseRequestAdapter(mContext: Context, mActivity: Activity, private val updateData: ((ArrayList<SalesInfoModel>, SapModel) -> Unit)): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = mContext
    var activity = mActivity
    var mSapList: ArrayList<SapModel>? = null // SAP Code 리스트
    var selectedSAP: SapModel? = null // 선택된 SAP 모델
    var itemList: ArrayList<SalesInfoModel> = ArrayList() // 제품 리스트
    var selectedItem: SearchItemModel? = null // 선택된 제품을 담는 리스트
    var popupSearchResult : PopupSearchResult? = null // 제품 검색 팝업
    var historyList: List<ProductPriceHistoryModel>? = null // 제품 단가 이력 리스트
    var popupProductPriceHistory : PopupProductPriceHistory? = null // 제품 단가 이력 팝업
    var onItemSelect: ((SalesInfoModel) -> Unit)? = null // 선택된 제품 팝업
    var popupResultNothing : PopupNotice? = null // 조회 내역 없을 때
    var popupSAP: PopupSAP? = null //SAP Code 팝업

    private var sapCustomerCd: String ? = null // sap 거래처 코드
    private var sapCustomerNm : String? = null // sap 거래처 명
    private var arriveCd: String? = null // 배송처 코드
    private var arriveNm: String? = null // 배송처 명

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
        mLoginInfo = Utils.getLoginData()!!
        popupResultNothing = PopupNotice(context, context.getString(R.string.error))
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(HeaderPurchaseRequesetBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellOrderRegBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(itemList[position - 1]) // 헤더가 있으므로 position - 1
                val data = itemList[position-1]

                holder.binding.deleteButton.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        val popupDoubleMessage = PopupDoubleMessage(v.context, "제품 삭제", data.itemNm!!, "선택한 제품이 주문리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                            override fun onCancelClick() {
                                Utils.Log("취소 클릭함")
                            }

                            @SuppressLint("NotifyDataSetChanged")
                            override fun onOkClick() {
                                removeItem(data, selectedSAP!!)
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
        return itemList.size + 1 // 헤더뷰를 포함
    }

    class ViewHolder(val binding: CellOrderRegBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: SalesInfoModel) {
            // 데이터 바인딩
            // 예: binding.textView.text = data.someText

            /*binding.tvBox.text = item.box
            binding.tvEach.text = item.each
            binding.tvPrice.text = "${item.unitPrice}원"
            binding.tvTotal.text = item.totalQty
            binding.tvTotalAmount.text = "${item.totalAmount}원"*/
        }
    }

    inner class HeaderViewHolder(private val binding: HeaderPurchaseRequesetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

            binding.sapCode.isSelected = true
            binding.shipping.isSelected = true

            Utils.Log("itemList ====> ${Gson().toJson(itemList)}")

            binding.accountArea.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (itemList.isNotEmpty() || binding.searchResult.text != context.getString(R.string.searchResult)) {
                        val popupNoticeV2 = PopupNoticeV2(v.context, "기존 주문이 완료되지 않았습니다.\n새로운 SAP Code를 검색하시겠습니까?",
                            object : Handler(Looper.getMainLooper()) {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun handleMessage(msg: Message) {
                                    when(msg.what) {
                                        Define.OK -> {
                                            clear()
                                            binding.etProductName.text = null
                                            binding.searchResult.text = v.context.getString(R.string.searchResult)
                                            binding.tvProductName.text = null
                                            binding.tvProductName.visibility = View.GONE
                                            binding.etProductName.visibility = View.VISIBLE
                                            binding.etProductName.hint = v.context.getString(R.string.productNameHint)
                                            notifyDataSetChanged()
                                            getSAPCode()
                                        }
                                    }
                                }
                            })
                        popupNoticeV2.show()
                    } else {
                        getSAPCode()
                    }
                }
            })

            binding.addressArea.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (binding.sapCode.text == context.getString(R.string.purchaseAccountHint)) {
                        popupResultNothing = PopupNotice(context, "거래처를 먼저 검색해주세요")
                        popupResultNothing?.show()
                    } else {
                        if (itemList.isNotEmpty() || binding.searchResult.text != context.getString(R.string.searchResult)) {
                            val popupNoticeV2 = PopupNoticeV2(v.context, "기존 주문이 완료되지 않았습니다.\n새로운 배송처를 검색하시겠습니까?",
                                object : Handler(Looper.getMainLooper()) {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun handleMessage(msg: Message) {
                                        when(msg.what) {
                                            Define.OK -> {
                                                binding.sapCode.text = null
                                                binding.etProductName.text = null
                                                binding.searchResult.text = v.context.getString(R.string.searchResult)
                                                binding.tvProductName.text = null
                                                binding.tvProductName.visibility = View.GONE
                                                binding.etProductName.visibility = View.VISIBLE
                                                binding.etProductName.hint = v.context.getString(R.string.productNameHint)
                                                clear()
                                                notifyDataSetChanged()
                                            }
                                        }
                                    }
                                })
                            popupNoticeV2.show()
                        } else {
                            getShipping(sapCustomerCd!!)
                        }
                    }
                }
            })

            binding.etProductName.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (sapCustomerCd.isNullOrEmpty()) {
                        PopupNotice(context, "거래처를 검색해주세요").show()
                    }
                }
            })

            binding.btSearch.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (sapCustomerCd.isNullOrEmpty()){
                        PopupNotice(context, "거래처를 검색해주세요").show()
                    } else {
                        if (binding.etProductName.text.isNullOrEmpty()) {
                            Toast.makeText(v.context, "제품명을 입력해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            // 아이템 리스트 검색
                            searchItem(binding.etProductName.text.toString(), binding.root.context)
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
                binding.etPrice.setText(it.netPrice.toString())
            }

            binding.btAddOrder.setOnClickListener(object : OnSingleClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSingleClick(v: View) {
                    if (binding.etBox.text.isNullOrEmpty() || binding.etPrice.text.isNullOrEmpty() || binding.searchResult.text == "검색된 제품명") {
                        Toast.makeText(v.context, "모든 항목을 채워주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            val itemName = binding.searchResult.text.toString()
                            val itemCd = selectedItem?.itemCd
                            val boxQty = Utils.getIntValue(binding.etBox.text.toString())

                            if (boxQty == 0) {
                                Toast.makeText(v.context, "박스에는 0이 들어갈 수 없습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                if (boxQty == 0) {
                                    Toast.makeText(
                                        v.context,
                                        "박스 수량을 확인해주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val addedItem = SalesInfoModel(
                                        itemNm = itemName,
                                        itemCd = itemCd!!,
                                        netPrice = selectedItem?.orderPrice,
                                        getBox = selectedItem?.getBox!!.toString(),
                                        boxQty = boxQty
                                    )
                                    selectedSAP = SapModel(
                                        sapCustomerCd = sapCustomerCd,
                                        sapCustomerNm = sapCustomerNm,
                                        arriveCd = arriveCd,
                                        arriveNm = arriveNm
                                    )

                                    addItem(addedItem, sapModel = selectedSAP!!)

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
                            Utils.Log("e ====> $e")
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

        // SAP Code 조회
        private fun getSAPCode() {
            val service = ApiClientService.retrofit.create(ApiClientService::class.java)
            //val call = service.sapCode(mLoginInfo.agencyCd!!, mLoginInfo.userId!!)

            //RETURN_CD_00 배송처 없음
            //val call = service.sapCode("C000028", "mb2004")

            //RETURN_CD_90 SAP코드 N개
            val call = service.sapCode("C000537", "mb2004")

            //RETURN_CD_91 SAP 코드 1개 & 배송처 코드 N개
            //val call = service.sapCode("C000032", "mb2004")

            call.enqueue(object : retrofit2.Callback<ListResultModel<SapModel>> {
                @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
                override fun onResponse(
                    call: Call<ListResultModel<SapModel>>,
                    response: Response<ListResultModel<SapModel>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        mSapList = item?.data as ArrayList<SapModel>
                        popupSAP = PopupSAP(context, mSapList!!, item.returnCd!!)
                        when(item.returnCd) {
                            // SAP코드 1개 & 배송처 코드 1개
                            Define.RETURN_CD_00 -> {
                                Utils.Log("return code : ${item.returnCd}\nSAP코드 1개 & 배송처 코드 1개")
                                sapCustomerCd = item.data.firstOrNull()?.sapCustomerCd
                                sapCustomerNm = item.data.firstOrNull()?.sapCustomerNm
                                arriveNm = item.data.firstOrNull()?.arriveNm
                                arriveCd = item.data.firstOrNull()?.arriveCd
                                Utils.Log("sap 거래처 코드 : $sapCustomerCd\nsap 거래처 명 : $sapCustomerNm\n배송처 코드 : $arriveCd\n배송처 명 : $arriveNm ")

                                val popupOk = PopupOk(context, "조회가 완료되었습니다.")
                                popupOk.show()
                                binding.sapCode.text = "($sapCustomerCd) $sapCustomerNm"
                                binding.shipping.text = context.getString(R.string.NoAddress)
                            }

                            // SAP 코드 N개
                            Define.RETURN_CD_90 -> {
                                Utils.Log("return code : ${item.returnCd}\nSAP 코드 N개")
                                Utils.Log("sap list ====> ${item.data}")
                                popupSAP?.show()

                                popupSAP?.onItemSelect = {
                                    sapCustomerNm = it.sapCustomerNm
                                    sapCustomerCd = it.sapCustomerCd
                                    binding.sapCode.text = "($sapCustomerCd) $sapCustomerNm"
                                    getShipping(sapCustomerCd!!)
                                    /*arriveNm = it.arriveNm
                                    arriveCd = it.arriveCd

                                    binding.sapCode.text = "(${it.sapCustomerCd}) ${it.sapCustomerNm}"
                                    if (arriveCd == "-" && arriveNm == "-") {
                                        binding.shipping.text = context.getString(R.string.NoAddress)
                                    } else {
                                        binding.shipping.text = "($arriveCd) $arriveNm"
                                    }*/
                                }
                            }

                            // SAP 코드 1개 & 배송처 코드 N개
                            Define.RETURN_CD_91 -> {
                                Utils.Log("return code : ${item.returnCd}\nSAP 코드 1개 & 배송처 코드 N개")
                                popupSAP?.show()

                                // 팝업 선택 시
                                popupSAP?.onItemSelect = {
                                    sapCustomerNm = it.sapCustomerNm
                                    sapCustomerCd = it.sapCustomerCd
                                    arriveNm = it.arriveNm
                                    arriveCd = it.arriveCd

                                    binding.sapCode.text = "(${it.sapCustomerCd}) ${it.sapCustomerNm}"
                                    if (arriveCd == "-" && arriveNm == "-") {
                                        binding.shipping.text = context.getString(R.string.NoAddress)
                                    } else {
                                        binding.shipping.text = "($arriveCd) $arriveNm"
                                    }
                                }
                            }

                            else -> {
                                PopupNotice(context, item.returnMsg).show()
                                Utils.Log("$item ====> ${item.returnMsg}")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ListResultModel<SapModel>>, t: Throwable) {
                    Utils.Log("sap search failed ====> ${t.message}")
                }

            })
        }

        fun getShipping(sapCustomerCd : String) {
            val service = ApiClientService.retrofit.create(ApiClientService::class.java)
            //val call = service.shipping(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, sapCustomerCd)

            //RETURN_CD_00 배송처 1개
            //val call = service.shipping("C000537", "mb2004", sapCustomerCd)

            //RETURN_CD_90 배송처 없음
            val call = service.shipping("C000537", "mb2004", sapCustomerCd)

            //RETURN_CD_91 배송처 코드 N개
            //val call = service.shipping("C000537", "mb2004", sapCustomerCd)

            call.enqueue(object: retrofit2.Callback<ListResultModel<SapModel>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<ListResultModel<SapModel>>,
                    response: Response<ListResultModel<SapModel>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        when(item?.returnCd) {
                            //배송처 1개
                            Define.RETURN_CD_00 -> {
                                Utils.Log("return code : ${item.returnCd}")
                                Utils.Log("returnMsg : ${item.returnMsg}")
                                arriveNm = item.data?.firstOrNull()?.arriveNm
                                arriveCd = item.data?.firstOrNull()?.arriveCd
                                Utils.Log("배송처 명 : $arriveNm\n배송처 코드 : $arriveCd")
                                if (arriveCd == "-" && arriveNm == "-") {
                                    binding.shipping.text = context.getString(R.string.NoAddress)
                                } else {
                                    binding.shipping.text = "($arriveCd) $arriveNm"
                                }
                            }

                            //배송처 없음
                            Define.RETURN_CD_90 -> {
                                Utils.Log("return code : ${item.returnCd}")
                                Utils.Log("returnMsg : ${item.returnMsg}")
                                arriveNm = item.data?.firstOrNull()?.arriveNm
                                arriveCd = item.data?.firstOrNull()?.arriveCd
                                Utils.Log("배송처 명 : $arriveNm\n배송처 코드 : $arriveCd")
                                binding.shipping.text = context.getString(R.string.NoAddress)
                            }

                            //배송처 N개
                            Define.RETURN_CD_91 -> {
                                Utils.Log("return code : ${item.returnCd}")
                                Utils.Log("returnMsg : ${item.returnMsg}")
                                mSapList = item.data as ArrayList<SapModel>
                                popupSAP = PopupSAP(context, mSapList!!, item.returnCd)
                                popupSAP?.show()
                                // 팝업 선택 시
                                popupSAP?.onItemSelect = {
                                    arriveNm = it.arriveNm
                                    arriveCd = it.arriveCd
                                    Utils.Log("배송처 명 : $arriveNm\n배송처 코드 : $arriveCd")
                                    binding.sapCode.text = "(${it.sapCustomerCd}) ${it.sapCustomerNm}"
                                    if (arriveCd == "-" && arriveNm == "-") {
                                        binding.shipping.text = context.getString(R.string.NoAddress)
                                    } else {
                                        binding.shipping.text = "($arriveCd) $arriveNm"
                                    }
                                }
                            }
                            else -> {
                                PopupNotice(context, item?.returnMsg!!).show()
                                binding.shipping.text = context.getString(R.string.purchaseAddressHint)
                                Utils.Log("$item ====> ${item.returnMsg}")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ListResultModel<SapModel>>, t: Throwable) {
                    Utils.Log("shipping search failed ====> ${t.message}")
                }

            })
        }

        // 단가 정보 조회
        fun searchItemPriceHistory(){
            val service = ApiClientService.retrofit.create(ApiClientService::class.java)
            val call = service.history(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, sapCustomerCd!!, selectedItem!!.itemCd!!)

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
            val orderYn = Define.PURCHASE_YES

            val call = service.item(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, sapCustomerCd!!, searchType, orderYn, searchCondition)

            call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<SearchItemModel>>> {
                override fun onResponse(
                    call: Call<ObjectResultModel<DataModel<SearchItemModel>>>,
                    response: Response<ObjectResultModel<DataModel<SearchItemModel>>>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()
                        if (item?.returnMsg == Define.SUCCESS) {
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
                                    binding.etPrice.text = Utils.decimal(it.orderPrice!!)
                                    selectedItem = SearchItemModel(
                                        itemCd = it.itemCd,
                                        itemNm = it.itemNm,
                                        whStock = it.whStock,
                                        getBox = it.getBox,
                                        vatYn = it.vatYn,
                                        netPrice = it.netPrice,
                                        enableOrderYn = it.enableOrderYn,
                                        orderPrice = it.orderPrice
                                    )
                                    Utils.Log("PurchaseRequestAdapter selected item ====> ${Gson().toJson(selectedItem)}")
                                }
                            }
                        } else {
                            popupResultNothing?.show()
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
    fun addItem(item: SalesInfoModel, sapModel: SapModel) {
        itemList.add(item)
        notifyDataSetChanged()
        updateData(itemList, sapModel)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(item: SalesInfoModel, sapModel: SapModel) {
        itemList.remove(item)
        notifyDataSetChanged()
        updateData(itemList, sapModel)
    }

    fun clear(item: SalesInfoModel? = null) {
        itemList.clear()
        updateData(itemList, selectedSAP!!)
    }
}