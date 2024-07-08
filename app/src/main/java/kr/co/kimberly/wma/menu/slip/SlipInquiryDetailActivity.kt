package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipInquiryDetailAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActSlipInquiryDetailBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.SalesInfoModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception

class SlipInquiryDetailActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryDetailBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel

    private var orderSlipList: ArrayList<SearchItemModel>? = null
    private var customerCd: String? = null
    private var customerNm: String? = null
    private var enableButtonYn: String? = null
    private var totalAmount: Int? = null
    private var slipNo: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!

        slipNo = intent.getStringExtra("slipNo")
        customerCd = intent.getStringExtra("customerCd")
        customerNm = intent.getStringExtra("customerNm")
        enableButtonYn = intent.getStringExtra("enableButtonYn")
        totalAmount = intent.getIntExtra("totalAmount", 0)
        orderSlipList = intent.getSerializableExtra("list") as ArrayList<SearchItemModel>

        Utils.Log("SlipInquiryDetailActivity\nslipNo ====> $slipNo\ncustomerCd ====> $customerCd\ncustomerNm ====> $customerNm\nenableButtonYn ====> $enableButtonYn\ntotalAmount ====> $totalAmount\norderSlipList ====> ${Gson().toJson(orderSlipList)}")

        showList()
        setUi()
        onClickPrint()

        if (enableButtonYn == "Y") {
            mBinding.delete.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    deleteSlip()
                }
            })
            mBinding.modify.setOnClickListener(object: OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    moveToEditPage()
                }
            })
        } else {
            mBinding.modify.visibility = View.GONE
            mBinding.delete.visibility = View.GONE
        }
    }

    private fun deleteSlip() {
        val popupDoubleMessage = PopupDoubleMessage(mContext, "주문전표삭제", "주문번호: ${mBinding.receiptNumber.text}", "선택한 전표가 전표 리스트에서 삭제됩니다.\n삭제하시겠습니까?")
        popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
            override fun onCancelClick() {
                Utils.Log("취소 클릭")
            }

            override fun onOkClick() {
                delete()
            }
        }
        popupDoubleMessage.show()
    }

    private fun onClickPrint() {
        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : ($customerCd) $customerNm\n총금액: ${Utils.decimal(totalAmount!!)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                    override fun onCancelClick() {
                        Utils.Log("취소 클릭")
                    }

                    override fun onOkClick() {
                        val intent = Intent(mContext, PrinterOptionActivity::class.java).apply {
                            //putExtra("slipNo", slipNo)
                            putExtra("slipNo", "20240600015")
                            putExtra("customerCd", customerCd)
                            putExtra("customerNm", customerNm)
                            putExtra("totalAmount", totalAmount)
                            putExtra("orderSlipList", orderSlipList)
                        }
                        startActivity(intent)
                        Toast.makeText(v.context, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                popupDoubleMessage.show()
            }
        })
    }
    private fun moveToEditPage() {
        val intent = Intent(mContext, SlipInquiryModifyActivity::class.java).apply {
            //putExtra("slipNo", slipNo)
            putExtra("slipNo", "20240600015")
            putExtra("customerCd", customerCd)
            putExtra("customerNm", customerNm)
            putExtra("enableButtonYn", enableButtonYn)
            putExtra("totalAmount", totalAmount)
            putExtra("orderSlipList", orderSlipList)
        }
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun setUi() {
        mBinding.header.headerTitle.text = getString(R.string.menu04)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.bottom.bottomButton.text = getString(R.string.slipPrint)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.receiptNumber.text = slipNo
        mBinding.accountName.text = "($customerCd) $customerNm"
    }

    @SuppressLint("SetTextI18n")
    private fun showList() {
        mBinding.tvTotalAmount.text = "${Utils.decimal(totalAmount!!)}원"
        val adapter = SlipInquiryDetailAdapter(mContext) { _, _ -> }
        adapter.dataList = orderSlipList!!
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }

    private fun delete() {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val deliveryDate = Utils.getCurrentDateFormatted()

        val json = JsonObject().apply {
            addProperty("agencyCd", mLoginInfo.agencyCd)
            addProperty("userId", mLoginInfo.userId)
            addProperty("slipNo", slipNo)
            addProperty("slipType", Define.ORDER)
            addProperty("customerCd", customerCd)
            addProperty("preSalesType", "N")
            addProperty("totalAmount", totalAmount)
        }

        Utils.Log("final delete json ====> ${Gson().toJson(json)}")

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.delete(body)

        call.enqueue(object : retrofit2.Callback<ListResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ListResultModel<DataModel<Unit>>>,
                response: Response<ListResultModel<DataModel<Unit>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        Utils.Log("delete success ====> ${Gson().toJson(item)}")
                        Toast.makeText(mContext, "전표가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        Intent().putExtra("deletedSlipNo", slipNo).apply {
                            setResult(Activity.RESULT_OK, this)
                        }
                        finish()
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<DataModel<Unit>>>, t: Throwable) {
                Utils.Log("delete failed ====> ${t.message}")
            }
        })
    }
}