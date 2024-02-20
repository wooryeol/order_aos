package kr.co.kimberly.wma.menu.slip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.adapter.MainMenuAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.custom.GridSpacingItemDecoration
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNotification
import kr.co.kimberly.wma.databinding.ActMainBinding
import kr.co.kimberly.wma.databinding.ActSlipInquiryBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.MainMenuModel

class SlipInquiryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val collectList = ArrayList<AccountModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu04)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        // 거래처 미수금 검색
        mBinding.search.setOnClickListener {
            showCollectList()
        }
    }

    private fun showCollectList() {
        collectList.add(AccountModel("202312000131", "(000020) 경주마트", "30,000"))

        val adapter = CollectListAdapter(mContext, mActivity)
        adapter.dataList = collectList
        adapter.isSlipAct = true // 전표조회에서 진입했다는 걸 알려줌
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (collectList.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE
        } else {
            mBinding.noSearch.visibility = View.VISIBLE
            mBinding.recyclerview.visibility = View.GONE
        }
    }
}