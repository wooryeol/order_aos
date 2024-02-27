package kr.co.kimberly.wma.menu.slip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.adapter.MainMenuAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.custom.GridSpacingItemDecoration
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNotification
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.ActMainBinding
import kr.co.kimberly.wma.databinding.ActSlipInquiryBinding
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.menu.`return`.ReturnRegActivity
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.MainMenuModel
import kr.co.kimberly.wma.model.SearchResultModel

class SlipInquiryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val collectList = ArrayList<AccountModel>()
    val list = ArrayList<SearchResultModel>()

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
        mBinding.btSearch.setOnClickListener {
            showCollectList()
        }

        showImageButton()

        mBinding.btAccountNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.etAccountName.text = null
                mBinding.tvAccountName.text = null
                mBinding.tvAccountName.visibility = View.GONE
                mBinding.etAccountName.visibility = View.VISIBLE
                mBinding.etAccountName.hint = v.context.getString(R.string.productNameHint)
            }
        })

        mBinding.btSearch.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mBinding.root.context)
                popupAccountSearch.onItemSelect = {
                    mBinding.tvAccountName.text = it.name
                    /*OrderRegActivity.accountName = it.name
                    OrderRegActivity.list.clear()
                    ReturnRegActivity.accountName = it.name
                    ReturnRegActivity.list.clear()*/
                }
                popupAccountSearch.show()
            }
        })
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

    private fun showImageButton() {
        mBinding.etAccountName.addTextChangedListener {
            if (mBinding.etAccountName.text.isNullOrEmpty()) {
                mBinding.btAccountNameEmpty.visibility = View.GONE
            } else {
                mBinding.btAccountNameEmpty.visibility = View.VISIBLE
            }
        }
    }
}