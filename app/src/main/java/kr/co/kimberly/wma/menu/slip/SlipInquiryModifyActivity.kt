package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipInquiryModifyAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
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
    private var orderSlipList: ArrayList<SearchItemModel>? = null // 오더 리스트
    private var customerCd: String? = null
    private var customerNm: String? = null
    private var totalAmount: Int = 0
    private var slipNo: String? = null

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mLoginInfo = Utils.getLoginData()!!

        slipNo = intent.getStringExtra("slipNo")
        customerCd = intent.getStringExtra("customerCd")
        customerNm = intent.getStringExtra("customerNm")
        totalAmount = intent.getIntExtra("totalAmount", 0)
        orderSlipList = intent.getSerializableExtra("orderSlipList") as ArrayList<SearchItemModel>
        originSlipList = arrayListOf()
        originSlipList.addAll(orderSlipList!!)
        getItemCode(orderSlipList!!)

        Utils.Log("SlipInquiryModifyActivity\nslipNo ====> $slipNo\ncustomerCd ====> $customerCd\ncustomerNm ====> $customerNm\ntotalAmount ====> $totalAmount\norderSlipList ====> ${Gson().toJson(orderSlipList)}")

        setUi()
        onClickApprovalOrder()

        modifyAdapter = SlipInquiryModifyAdapter(mContext, customerCd!!, customerNm!!) {items ->
            totalAmount = 0

            items.map {
                val stringWithoutComma = it.amount.toString().replace(",", "")
                totalAmount += stringWithoutComma.toInt()
            }

            mBinding.tvTotalAmount.text = "${Utils.decimal(totalAmount!!)}원"
        }

        modifyAdapter?.slipList = orderSlipList
        mBinding.recyclerview.adapter = modifyAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBinding.header.backBtn.setOnClickListener(object : OnSingleClickListener(){
            override fun onSingleClick(v: View) {
                Utils.backBtnPopup(mContext, mActivity, orderSlipList!!)
            }
        })
    }
    @SuppressLint("SetTextI18n")
    private fun setUi() {
        mBinding.header.headerTitle.text = getString(R.string.slipModify)
        mBinding.bottom.bottomButton.text = getString(R.string.titleOrder)
        mBinding.tvTotalAmount.text = "${Utils.decimal(totalAmount!!)}원"

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })
    }

    private fun onClickApprovalOrder() {
        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : $customerNm\n총금액: ${Utils.decimal(totalAmount!!)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                if (!checkItem(orderSlipList, originSlipList)) {
                    Utils.popupNotice(mContext, "수정된 제품이 없습니다.")
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Utils.Log("취소 클릭함")
                        }

                        override fun onOkClick() {
                            updateOrder()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }

    private fun updateOrder(){
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val jsonArray = Gson().toJsonTree(orderSlipList).asJsonArray
        val deliveryDate = Utils.getCurrentDateFormatted()

        val json = JsonObject().apply {
            addProperty("agencyCd", mLoginInfo?.agencyCd)
            addProperty("userId", mLoginInfo?.userId)
            addProperty("slipNo", slipNo)
            addProperty("slipType", Define.ORDER)
            addProperty("customerCd", customerCd)
            addProperty("deliveryDate", deliveryDate)
            addProperty("preSalesType", "N")
            addProperty("totalAmount", totalAmount)
        }
        json.add("salesInfo", jsonArray)
        Utils.Log("final updated order json ====> ${Gson().toJson(json)}")

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.update(body)

        call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ObjectResultModel<DataModel<Unit>>>,
                response: Response<ObjectResultModel<DataModel<Unit>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    Utils.Log("item ===> ${Gson().toJson(item)}")
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.Log("order success ====> ${Gson().toJson(item)}")
                        val data = orderSlipList
                        val slipNo = item.data?.slipNo
                        Utils.toast(mContext, "주문이 전송되었습니다.")
                        Utils.Log("returnMsg ====> ${item.returnMsg}")
                        val intent = Intent(mContext, PrinterOptionActivity::class.java).apply {
                            putExtra("data", data)
                            putExtra("slipNo", slipNo)
                            putExtra("title", mContext.getString(R.string.titleOrder))
                        }
                        startActivity(intent)
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<DataModel<Unit>>>, t: Throwable) {
                Utils.Log("order failed ====> ${t.message}")
            }

        })
    }

    private fun getItemCode(orderSlipList: List<SearchItemModel>) {
        val pattern = Pattern.compile("\\((.*?)\\)")

        for (searchItemModel in orderSlipList) {
            val matcher = searchItemModel.itemNm?.let { pattern.matcher(it) }
            Utils.Log("searchItemModel.vatYn ====> ${searchItemModel.vatYn}")
            val supplyPrice = if (searchItemModel.vatYn == "01") {
                ceil(searchItemModel.amount!! / 1.1).toInt()
            } else {
                searchItemModel.amount!!
            }
            val vat = searchItemModel.amount - supplyPrice
            searchItemModel.supplyPrice = supplyPrice
            searchItemModel.vat = vat
            if (matcher!!.find()) {
                searchItemModel.itemCd = matcher.group(1)
            }
        }
    }

    fun checkItem(slipList: ArrayList<SearchItemModel>?, originSlipList: ArrayList<SearchItemModel>): Boolean {
        // 크기 비교
        if (slipList?.size != originSlipList.size) {
            return true
        }

        // itemCd 비교
        for (i in slipList.indices) {
            val modifyItemCd = slipList[i].itemCd
            val originItemCd = originSlipList[i].itemCd

            if (modifyItemCd != originItemCd) {
                return true
            }
        }
        return false
    }
}