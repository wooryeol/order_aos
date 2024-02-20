package kr.co.kimberly.wma.menu.slip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.adapter.RegAdapter
import kr.co.kimberly.wma.adapter.SlipInquiryDetailAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.ActSlipInquiryDetailBinding
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.OrderRegModel

class SlipInquiryDetailActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryDetailBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val collectList = ArrayList<AccountModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu04)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.bottom.bottomButton.text = getString(R.string.slipPrint)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.modify.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val intent = Intent(mContext, SlipInquiryModifyActivity::class.java)
                startActivity(intent)
            }
        })

        val list = ArrayList<OrderRegModel>()
        for(i: Int in 1..10) {
            list.add(OrderRegModel("(34870) 하기스프리미어 3공 100/1", "10", "0", "9,999,999원", "240", "9,999,999,999원"))
        }

        val adapter = SlipInquiryDetailAdapter(mContext)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}