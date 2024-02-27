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
import kr.co.kimberly.wma.custom.TotalValueListener
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActPurchaseRequestBinding
import kr.co.kimberly.wma.model.OrderRegModel
import java.text.DecimalFormat

class PurchaseRequestActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseRequestBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    val decimal = DecimalFormat("#,###")

    companion object {
        val list = ArrayList<OrderRegModel>()
        @SuppressLint("StaticFieldLeak")
        var purchaseAdapter: PurchaseRequestAdapter? = null
        var accountName = ""
        var totalAmount = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseRequestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu08)
        mBinding.bottom.bottomButton.text = getString(R.string.menu08)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                list.clear()
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "발주전송", "SAP Name : $accountName\n총금액 : ${decimal.format(totalAmount)}원", getString(R.string.purchasePostMsg03), true)

                if (list.isEmpty()) {
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
        purchaseAdapter = PurchaseRequestAdapter(mContext, mActivity, object : TotalValueListener {
            override fun onTotalValueChanged(totalValue: Int) {
                mBinding.tvTotalAmount.text = "${decimal.format(totalValue)}원"
                totalAmount = totalValue
            }

        })
        purchaseAdapter?.dataList = list
        mBinding.recyclerview.adapter = purchaseAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }

    override fun onResume() {
        super.onResume()
        list.clear()
    }
}