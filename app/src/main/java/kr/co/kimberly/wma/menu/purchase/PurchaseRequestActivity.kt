package kr.co.kimberly.wma.menu.purchase

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
import kr.co.kimberly.wma.adapter.PurchaseRequestAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActPurchaseRequestBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response

class PurchaseRequestActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseRequestBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel

    var purchaseAdapter: PurchaseRequestAdapter? = null
    private var totalAmount: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseRequestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!

        mBinding.header.headerTitle.text = getString(R.string.menu08)
        mBinding.bottom.bottomButton.text = getString(R.string.menu08)

        purchaseAdapter = PurchaseRequestAdapter(mContext, mActivity) { itemList, item ->
            totalAmount = 0
            itemList.map {
                val stringWithoutComma = it.amount.toString().replace(",", "")
                totalAmount += stringWithoutComma.toInt()
            }

            val formatTotalMoney = Utils.decimal(totalAmount)
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"
        }

        mBinding.recyclerview.adapter = purchaseAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                Utils.backBtnPopup(mContext, mActivity, purchaseAdapter?.itemList!!)
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "발주전송", "SAP Name : ${purchaseAdapter?.selectedSAP?.sapCustomerNm}\n총금액 : ${Utils.decimal(totalAmount!!)}원", getString(R.string.purchasePostMsg03), true)

                if (purchaseAdapter?.itemList!!.isEmpty()) {
                    Utils.popupNotice(mContext, "제품이 등록되지 않았습니다.")
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Utils.Log("취소 클릭")
                        }

                        override fun onOkClick() {
                            postOrderSlip()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }

    private fun postOrderSlip() {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val sapModel = purchaseAdapter?.selectedSAP

        val agencyCd = mLoginInfo.agencyCd
        val userId = mLoginInfo.userId
        val sapCustomerCd = sapModel?.sapCustomerCd
        val arriveCd = sapModel?.arriveCd
        val slipType = Define.ORDER
        val orderDate = Utils.getCurrentDateFormatted()
        val deliveryDate = Utils.getCurrentDateFormatted()
        val totalAmount = totalAmount
        val jsonArray = Gson().toJsonTree(purchaseAdapter?.itemList).asJsonArray

        val json = JsonObject().apply {
            //test
            //addProperty("agencyCd", agencyCd)
            addProperty("agencyCd", "C000032")
            addProperty("userId", userId)
            addProperty("sapCustomerCd", sapCustomerCd)
            addProperty("arriveCd", arriveCd)
            addProperty("slipType", slipType)
            addProperty("orderDate", orderDate)
            addProperty("orderDate", orderDate)
            addProperty("deliveryDate", deliveryDate)
            addProperty("totalAmount", totalAmount)
        }
        json.add("orderInfo", jsonArray)

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        Utils.Log("purchase request body ====> ${Gson().toJson(json)}")
        val call = service.headOfficeOrderSlip(body)

        call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ObjectResultModel<DataModel<Unit>>>,
                response: Response<ObjectResultModel<DataModel<Unit>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.Log("purchase success")
                        Utils.Log("item ====> ${item.data}")
                        val intent = Intent(mContext, PurchaseApprovalActivity::class.java).apply {
                            putExtra("slipNo", item.data?.slipNo)
                            putExtra("sapModel", sapModel)
                            putExtra("purchaseList", purchaseAdapter?.itemList)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "로그인 정보를 확인해주세요")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<DataModel<Unit>>>, t: Throwable) {
                Utils.Log("purchase failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }
}