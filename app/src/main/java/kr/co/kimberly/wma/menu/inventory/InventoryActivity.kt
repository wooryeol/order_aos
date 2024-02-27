package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.ActInventoryBinding
import kr.co.kimberly.wma.model.InventoryModel
import kr.co.kimberly.wma.model.SearchResultModel


class InventoryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInventoryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInventoryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        // 초기 셋팅
        setSetting()

        // 헤더 설정 변경
        mBinding.header.headerTitle.text = getString(R.string.menu06)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        // 진입 시 창고 리스트 팝업 활성화
        mBinding.tvBranchHouse.setOnClickListener {
            val list = ArrayList<SearchResultModel>()

            for(i: Int in 1..15) {
                list.add(SearchResultModel("(I00$i) 기본창고"))
            }

            val popupSearchResult = PopupSearchResult(mContext, list)
            popupSearchResult.onItemSelect = {
                showInventoryList()
            }
            popupSearchResult.show()
        }

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.accountName.text.toString().isEmpty()) {
                    showNotice(getString(R.string.accountHint))
                } else {
                    val list = ArrayList<SearchResultModel>()

                    for(i: Int in 1..15) {
                        list.add(SearchResultModel("(I00$i) 기본창고"))
                    }

                    val popupSearchResult = PopupSearchResult(mContext, list)
                    popupSearchResult.onItemSelect = {
                        showInventoryList()
                    }
                    popupSearchResult.show()
                }
            }
        })
    }

    private fun setSetting() {
        // 화면 진입 시 대리점 팝업 노출
        val list = ArrayList<SearchResultModel>()

        for(i: Int in 1..15) {
            list.add(SearchResultModel("(I00$i) 기본창고"))
        }

        val popupSearchResult = PopupSearchResult(mContext, list)
        popupSearchResult.onItemSelect = {
            mBinding.tvBranchHouse.text = it.name
        }
        popupSearchResult.show()

        // 텍스트를 흘러가게 하기 위함
        mBinding.tvBranchHouse.isSelected = true
    }

    // 검색을 눌렀을 때
    private fun showInventoryList() {
        val inventoryList = ArrayList<InventoryModel>()
        for(i: Int in 1..15) {
            inventoryList.add(InventoryModel("(00234)크리넥스 수앤수 10매*3", "20", "0", "1000"))
        }

        val adapter = InventoryListAdapter(mContext, mActivity)
        adapter.dataList = inventoryList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (inventoryList.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(mBinding.accountName.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            mBinding.accountName.clearFocus()
        } else {
            showNotice(getString(R.string.searchNothing))
        }
    }

    private fun showNotice(msg: String) {
        val popupNotice = PopupNotice(mContext, msg)
        popupNotice.show()
    }
}