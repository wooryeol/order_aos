package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipInquiryModifyAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.OrderRegModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import java.text.DecimalFormat

class SlipInquiryModifyActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel

    private var orderSlipList: ArrayList<SearchItemModel>? = null
    private var customerCd: String? = null
    private var customerNm: String? = null
    private var totalAmount: Int? = null
    private var slipNo: String? = null

    @SuppressLint("SetTextI18n")
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

        Utils.Log("SlipInquiryModifyActivity\nslipNo ====> $slipNo\ncustomerCd ====> $customerCd\ncustomerNm ====> $customerNm\ntotalAmount ====> $totalAmount\norderSlipList ====> ${Gson().toJson(orderSlipList)}")

        setUi()
        onClickApprovalOrder()


        val modifyAdapter = SlipInquiryModifyAdapter(mContext, customerCd!!, customerNm!!) {itemList ->
            /*items.map {
                val stringWithoutComma = it.totalAmount.replace(",", "")
                totalMoney += stringWithoutComma.toInt()
            }*/

           /* accountName = name.ifEmpty {
                accountName
            }
            totalAmount = totalMoney*/

            val formatTotalMoney = Utils.decimal(totalAmount!!)
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"
        }

        modifyAdapter.slipList = orderSlipList
        modifyAdapter.onItemClickedListener = object: SlipInquiryModifyAdapter.OnItemClickedListener {
            override fun onItemClicked(item: SearchItemModel) {
                modifyAdapter.headerUpdate(item)
                //modifyAdapter.headerViewHolder?.bind(item) // 헤더 뷰 홀더가 있으면 직접 바인딩
                // adapter.notifyItemChanged(0) // 헤더만 갱신
            }
        }

        mBinding.recyclerview.adapter = modifyAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
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

                popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                    override fun onCancelClick() {
                        Log.d("tttt", "취소 클릭함")
                    }

                    override fun onOkClick() {
                        Toast.makeText(v.context, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(mContext, PrinterOptionActivity::class.java))
                    }
                }

                popupDoubleMessage.show()
            }
        })
    }
}