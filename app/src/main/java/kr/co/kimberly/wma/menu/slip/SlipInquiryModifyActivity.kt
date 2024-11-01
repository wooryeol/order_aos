package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipInquiryModifyAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDeliveryDatePicker
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.db.DBHelper
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.util.regex.Pattern
import kotlin.math.ceil

class SlipInquiryModifyActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel
    private lateinit var originSlipList: ArrayList<SearchItemModel> // 기존 데이터 리스트

    private var modifyAdapter: SlipInquiryModifyAdapter? = null
    private lateinit var orderSlipList: ArrayList<SearchItemModel> // 오더 리스트
    private lateinit var customerCd: String
    private lateinit var customerNm: String
    private lateinit var slipNo: String

    private val db : DBHelper by lazy {
        DBHelper.getInstance(applicationContext)
    }
    private var isSave = true // 액티비티가 종료 될 때 이 값을 통해 저장 여부 선택

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()
        slipNo = intent.getStringExtra("slipNo")!!
        customerCd = intent.getStringExtra("customerCd")!!
        customerNm = intent.getStringExtra("customerNm")!!
        orderSlipList = intent.getSerializableExtra("orderSlipList") as ArrayList<SearchItemModel>
        originSlipList = arrayListOf()
        originSlipList.addAll(orderSlipList)
        getItemCode(orderSlipList)
        setUi()

        Utils.log("Slip Inquiry Modify Activity\nslipNo ====> $slipNo\ncustomerCd ====> $customerCd\ncustomerNm ====> $customerNm\ntotalAmount ====> ${totalAmount(orderSlipList)}\norderSlipList ====> ${Gson().toJson(orderSlipList)}")

        // 소프트키 뒤로가기
        this.onBackPressedDispatcher.addCallback(this, callback)

        // 헤더 뒤로가기
        mBinding.header.backBtn.setOnClickListener(object : OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                goBack()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "납기일자 선택", "납기 일자를 선택하시겠습니까?\n선택하지 않으시면 납기일자가 다음 날로 저장됩니다.")
                if (!checkItem(orderSlipList, originSlipList)) {
                    Utils.popupNotice(mContext, "수정된 제품이 없습니다.")
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Utils.log("취소 클릭 ====> 납기일자 다음 날로 설정")
                            checkOrderPopup(Utils.getNextDay())
                        }

                        override fun onOkClick() {
                            setDeliveryDate()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }

    // 어댑터 설정
    @SuppressLint("SetTextI18n")
    private fun setAdapter() {
        modifyAdapter = SlipInquiryModifyAdapter(mContext, orderSlipList, customerCd, customerNm) {items ->
            Utils.log("items ====> ${Gson().toJson(items)}")
            mBinding.tvTotalAmount.text = "${Utils.decimalLong(totalAmount(items))}원"
        }

        mBinding.recyclerview.adapter = modifyAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

    }

    // 수정 주문 확인 팝업
    private fun checkOrderPopup(deliveryDate: String) {
        val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : $customerNm\n총금액: ${Utils.decimalLong(totalAmount(orderSlipList))}원\n납기일자: $deliveryDate", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
            override fun onCancelClick() {
                Utils.log("취소 클릭함")
            }

            override fun onOkClick() {
                updateOrder(deliveryDate)
            }
        }
        popupDoubleMessage.show()
    }

    // 납기 일자 선택 팝업
    private fun setDeliveryDate() {
        val popupDeliveryDatePicker = PopupDeliveryDatePicker(mContext)
        popupDeliveryDatePicker.onSelectedDate = {
            checkOrderPopup(it)
        }
        popupDeliveryDatePicker.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setUi() {
        val data = db.slipList
        val dataList = arrayListOf<SearchItemModel>()

        data.forEach {
            if (it.slipNo == slipNo) {
                dataList.add(it)
            }
        }

        if (dataList.size > 0) {
            orderSlipList = dataList
        }

        setAdapter()

        mBinding.header.headerTitle.text = getString(R.string.slipModify)
        mBinding.bottom.bottomButton.text = getString(R.string.titleOrder)
        mBinding.tvTotalAmount.text = "${Utils.decimalLong(totalAmount(orderSlipList))}원"
    }

    private fun updateOrder(deliveryDate: String){
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val jsonArray = Gson().toJsonTree(orderSlipList).asJsonArray

        val json = JsonObject().apply {
            addProperty("agencyCd", mLoginInfo.agencyCd)
            addProperty("userId", mLoginInfo.userId)
            addProperty("slipNo", slipNo)
            addProperty("slipType", Define.ORDER)
            addProperty("customerCd", customerCd)
            addProperty("deliveryDate", deliveryDate)
            addProperty("preSalesType", "N")
            addProperty("totalAmount", totalAmount(orderSlipList))
        }
        json.add("salesInfo", jsonArray)
        Utils.log("final updated order json ====> ${Gson().toJson(json)}")

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.update(body)

        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ResultModel<DataModel<Unit>>>,
                response: Response<ResultModel<DataModel<Unit>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    Utils.log("item ===> ${Gson().toJson(item)}")
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.log("order success ====> ${Gson().toJson(item)}")
                        val data = orderSlipList
                        val newSlipNo = item.data.slipNo
                        Utils.toast(mContext, "주문이 전송되었습니다.")
                        Utils.log("returnMsg ====> ${item.returnMsg}")

                        // 주문이 전송되면 데이터 초기화
                        deleteData()

                        val intent = Intent(mContext, PrinterOptionActivity::class.java).apply {
                            //putExtra("data", data)
                            putExtra("slipNo", newSlipNo)
                            putExtra("title", mContext.getString(R.string.titleOrder))
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DataModel<Unit>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("order failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    private fun getItemCode(orderSlipList: List<SearchItemModel>) {
        val pattern = Pattern.compile("\\((.*?)\\)")

        for (searchItemModel in orderSlipList) {
            val matcher = searchItemModel.itemNm?.let { pattern.matcher(it) }
            val supplyPrice = if (searchItemModel.vatYn == "01") {
                ceil(searchItemModel.amount!! / 1.1).toLong()
            } else {
                searchItemModel.amount!!
            }
            val vat = searchItemModel.amount!! - supplyPrice
            searchItemModel.supplyPrice = supplyPrice
            searchItemModel.vat = vat
            if (matcher!!.find()) {
                searchItemModel.itemCd = matcher.group(1)
            }
        }

        // 전표번호 업데이트
        orderSlipList.forEach {
            it.slipNo = slipNo
        }
    }

    fun checkItem(slipList: ArrayList<SearchItemModel>?, originSlipList: ArrayList<SearchItemModel>): Boolean {
        // 크기 비교
        if (slipList?.size != originSlipList.size) {
            return true
        }

        // itemCd 비교
        for (i in slipList.indices) {
            val modifyItem = slipList[i]
            val originItem = originSlipList[i]

            if (modifyItem != originItem) {
                return true
            }
        }
        return false
    }

    private fun saveData() {
        db.deleteSlipData(slipNo)
        modifyAdapter?.slipList?.forEach {
            it.slipNo = slipNo
            db.insertSlipData(it)
        }
    }

    private fun deleteData() {
        isSave = false
        db.deleteSlipData(slipNo)
    }

    override fun onStop() {
        super.onStop()
        if (!modifyAdapter?.slipList.isNullOrEmpty() && isSave){
            saveData()
        }
    }

    private fun totalAmount(list: ArrayList<SearchItemModel>): Long {
        var total: Long = 0
        list.forEach {
            total += it.amount ?: 0
        }
        return total
    }

    // 뒤로가기 버튼
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    private fun goBack() {
        if (!checkItem(orderSlipList, originSlipList)){
            db.deleteSlipData(slipNo)
            finish()
        } else {
            // 수정 도중 나갈 경우
            if (!modifyAdapter?.slipList.isNullOrEmpty()) {
                PopupNoticeV2(mContext, "기존 수정이 완료되지 않았습니다.\n전표를 저장하시겠습니까?",
                    object : Handler(Looper.getMainLooper()) {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun handleMessage(msg: Message) {
                            when (msg.what) {
                                Define.EVENT_OK -> {
                                    saveData()
                                    finish()
                                }
                                Define.EVENT_CANCEL -> {
                                    SlipInquiryDetailActivity().dataList.clear()
                                    deleteData()
                                    finish()
                                }
                            }
                        }
                    }
                ).show()
            } else {
                deleteData()
                finish()
            }
        }
    }
}