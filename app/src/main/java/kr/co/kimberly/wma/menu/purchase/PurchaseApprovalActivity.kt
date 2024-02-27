package kr.co.kimberly.wma.menu.purchase

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipInquiryDetailAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.ActPurchaseApprovalBinding
import kr.co.kimberly.wma.model.OrderRegModel
import java.text.DecimalFormat

class PurchaseApprovalActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseApprovalBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val decimal = DecimalFormat("#,###")

    companion object {
        var purchaseAddress = ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseApprovalBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.PurchaseApproval)
        mBinding.header.scanBtn.visibility = View.GONE

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                startActivity(Intent(mContext, PurchaseRequestActivity::class.java))
                finish()
            }
        })

        mBinding.tvTotalAmount.text = "${decimal.format(PurchaseRequestActivity.totalAmount)}원"
        mBinding.accountCode.text = PurchaseRequestActivity.accountName
        mBinding.purchaseAddress.text = purchaseAddress

        val adapter = SlipInquiryDetailAdapter(mContext)
        adapter.dataList = PurchaseRequestActivity.list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}