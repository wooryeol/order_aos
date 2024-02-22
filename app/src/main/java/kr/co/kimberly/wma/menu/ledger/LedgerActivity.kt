package kr.co.kimberly.wma.menu.ledger

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.adapter.LedgerAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.databinding.ActLedgerBinding
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.LedgerModel

class LedgerActivity : AppCompatActivity() {

    private lateinit var mBinding: ActLedgerBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val ledgerList = ArrayList<LedgerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActLedgerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        // 헤더 설정
        mBinding.header.headerTitle.text = getString(R.string.menu05)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })


        // 날짜 선택
        val datePickerDialog = PopupDatePicker(this)
        mBinding.date.setOnClickListener {
            datePickerDialog.initCustomDatePicker(mBinding.date, true)
        }

        // 거래처 검색
        mBinding.btSearch.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mContext)
                popupAccountSearch.onItemSelect = {
                    mBinding.accountName.text = it.name
                }
                popupAccountSearch.show()
            }
        })
    }

    // 검색을 눌렀을 때
    private fun showCollectList() {
        ledgerList.add(LedgerModel("2023-12-07", "924,000원", "0원"))
        ledgerList.add(LedgerModel("2023-12-28", "0원", "510,000원"))

        val adapter = LedgerAdapter(mContext, mActivity)
        adapter.dataList = ledgerList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (ledgerList.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE
        } else {
            mBinding.noSearch.visibility = View.VISIBLE
            mBinding.recyclerview.visibility = View.GONE
        }
    }
}