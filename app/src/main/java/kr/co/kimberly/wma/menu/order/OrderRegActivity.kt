package kr.co.kimberly.wma.menu.order

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.RegAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import kr.co.kimberly.wma.network.model.SalesInfoModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response


class OrderRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보

    private var accountName = ""
    private var totalAmount = 0
    private var orderAdapter: RegAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()

        mBinding.header.headerTitle.text = getString(R.string.menu01)
        mBinding.bottom.bottomButton.text = getString(R.string.titleOrder)

        orderAdapter = RegAdapter(mContext, mActivity) { items, name ->
            var totalMoney = 0

            items.map {
                val stringWithoutComma = it.amount.toString().replace(",", "")
                totalMoney += stringWithoutComma.toInt()
            }

            accountName = name.ifEmpty {
                accountName
            }
            totalAmount = totalMoney

            val formatTotalMoney = Utils.decimal(totalMoney)
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"
        }
        mBinding.recyclerview.adapter = orderAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : $accountName\n총금액: ${Utils.decimal(totalAmount)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                if (orderAdapter?.dataList!!.isEmpty()) {
                    Toast.makeText(mContext, "제품이 등록되지 않았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Utils.Log("취소 클릭")
                        }

                        override fun onOkClick() {
                            order()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }

    private fun order(){
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val jsonArray = Gson().toJsonTree(orderAdapter?.dataList!!).asJsonArray
        val deliveryDate = Utils.getCurrentDateFormatted()

        val json = JsonObject().apply {
            addProperty("agencyCd", mLoginInfo?.agencyCd)
            addProperty("userId", mLoginInfo?.userId)
            addProperty("slipType", Define.ORDER)
            addProperty("customerCd", orderAdapter?.customerCd)
            addProperty("deliveryDate", deliveryDate)
            addProperty("preSalesType", "N")
            addProperty("totalAmount", totalAmount)
        }
        json.add("salesInfo", jsonArray)
        Utils.Log("final order json ====> ${Gson().toJson(json)}")

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.order(body)

        call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<SalesInfoModel>>> {
            override fun onResponse(
                call: Call<ObjectResultModel<DataModel<SalesInfoModel>>>,
                response: Response<ObjectResultModel<DataModel<SalesInfoModel>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    Utils.Log("item ===> ${Gson().toJson(item)}")
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.Log("order success ====> ${Gson().toJson(item)}")
                        val data = orderAdapter?.dataList
                        val slipNo = item.data?.slipNo
                        Toast.makeText(mContext, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(mContext, PrinterOptionActivity::class.java).apply {
                            putExtra("data", data)
                            putExtra("slipNo", slipNo)
                            putExtra("title", mContext.getString(R.string.titleOrder))
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        PopupNotice(mContext, item?.returnMsg!!).show()
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<DataModel<SalesInfoModel>>>, t: Throwable) {
                Utils.Log("order failed ====> ${t.message}")
            }

        })
    }
}