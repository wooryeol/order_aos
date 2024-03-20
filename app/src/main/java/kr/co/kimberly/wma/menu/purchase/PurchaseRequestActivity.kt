package kr.co.kimberly.wma.menu.purchase

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
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PurchaseRequestAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActPurchaseRequestBinding
import java.text.DecimalFormat

class PurchaseRequestActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseRequestBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity


    companion object {
        @SuppressLint("StaticFieldLeak")
        var purchaseAdapter: PurchaseRequestAdapter? = null
        var accountName = ""
        var purchaseAddress = ""
        var totalAmount = 0
    }

    private val decimal = DecimalFormat("#,###")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseRequestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu08)
        mBinding.bottom.bottomButton.text = getString(R.string.menu08)

        purchaseAdapter = PurchaseRequestAdapter(mContext, mActivity) { items, name, address ->
            var totalMoney = 0

            items.map {
                val stringWithoutComma = it.totalAmount.replace(",", "")
                totalMoney += stringWithoutComma.toInt()
            }

            accountName = name.ifEmpty {
                accountName
            }

            purchaseAddress = address.ifEmpty {
                purchaseAddress
            }
            totalAmount = totalMoney

            val formatTotalMoney = decimal.format(totalMoney).toString()
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"
        }

        mBinding.recyclerview.adapter = purchaseAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "발주전송", "SAP Name : $accountName\n총금액 : ${decimal.format(totalAmount)}원", getString(R.string.purchasePostMsg03), true)

                if (purchaseAdapter?.dataList!!.isEmpty()) {
                    Toast.makeText(mContext, "제품이 등록되지 않았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Log.d("tttt", "취소 클릭함")
                        }

                        override fun onOkClick() {
                            Toast.makeText(v.context, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext, PurchaseApprovalActivity::class.java))
                            finish()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }
}