package kr.co.kimberly.wma.menu.ledger

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.adapter.LedgerAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.custom.popup.PopupDatePicker02
import kr.co.kimberly.wma.databinding.ActLedgerBinding
import kr.co.kimberly.wma.network.model.AccountModel
import kr.co.kimberly.wma.network.model.LedgerModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        mBinding.dateArea.setOnClickListener {
            /*val date = if(mBinding.tvDate.text.toString() == getString(R.string.monthHint)) {
                null
            } else {
                mBinding.tvDate.text.toString()
            }
            val popupDatePicker = PopupDatePicker(mContext, true, date)
            popupDatePicker.onDateSelect = {
                mBinding.tvDate.text = it
            }
            popupDatePicker.show()*/

            val popupDatePicker = PopupDatePicker02(mContext, isDate = true, isStartDate = false)
            popupDatePicker.onSelectedDate = {
                mBinding.tvDate.text = it
            }
            popupDatePicker.show()
        }

        // 거래처 선택
        mBinding.accountArea.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mContext)
                popupAccountSearch.onItemSelect = {
                    mBinding.btEmpty.visibility = View.VISIBLE
                    mBinding.accountName.text = it.custNm
                }
                popupAccountSearch.show()
            }
        })

        mBinding.btSearch.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if (mBinding.tvDate.text.isNullOrEmpty()) {
                    Toast.makeText(mContext, "날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                } else if (mBinding.accountName.text.isNullOrEmpty()) {
                    Toast.makeText(mContext, "거래처를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    showCollectList()
                }
            }
        })
    }

    // 검색을 눌렀을 때
    private fun showCollectList() {
        showSummary()

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

    // 합계 나타내기
    private fun showSummary() {
        val list = arrayListOf(
            LedgerModel("2023-12-07", 924000, 0),
            LedgerModel("2023-12-14", 0, 510000),
            LedgerModel("2023-12-21", 1047800, 0),
            LedgerModel("2023-12-28", 0, 478000),

            LedgerModel("2024-01-01", 924000, 0),
            LedgerModel("2024-01-08", 0, 510000),
            LedgerModel("2024-01-15", 1047800, 0),
            LedgerModel("2024-01-22", 0, 478000),
            LedgerModel("2024-01-29", 670000, 0),

            LedgerModel("2024-02-05", 0, 850000),
            LedgerModel("2024-02-12", 1234000, 0),
            LedgerModel("2024-02-19", 0, 690000),
            LedgerModel("2024-02-26", 780000, 0),

            LedgerModel("2024-03-04", 0, 920000),
            LedgerModel("2024-03-11", 1420000, 0),
            LedgerModel("2024-03-18", 0, 760000),
            LedgerModel("2024-03-25", 890000, 0)
        )

        val selectedDate = "${mBinding.tvDate.text}-01"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatSelectedDate = LocalDate.parse(selectedDate, formatter)

        val previousBalance = sumAmount(formatSelectedDate, 2, list, false)
        val sumSalesAmount = sumAmount(formatSelectedDate, 1, list, true)
        val sumCollectAmount = sumAmount(formatSelectedDate, 1, list, false)

        mBinding.saleSum.text = Utils.decimal(sumSalesAmount)
        mBinding.performance.text =Utils.decimal(sumCollectAmount)
        mBinding.lastMonth.text = Utils.decimal(previousBalance)
        mBinding.balance.text = Utils.decimal(previousBalance+sumSalesAmount-sumCollectAmount)
    }
    private fun sumAmount(selectedDate: LocalDate, minusMonth: Long, list: List<LedgerModel>, sales: Boolean): Int {
        ledgerList.clear()
        var sumSalesAmount = 0 // 매출 합계
        var sumCollectAmount = 0 // 수금 실적
        var previousBalance = 0 // 전월 미수

        val previousMonth = selectedDate.minusMonths(minusMonth).monthValue
        val previousYear = if (previousMonth == 12) selectedDate.year - 1 else selectedDate.year

        for (item in list) {
            val ledgerDate = LocalDate.parse(item.date)
            val ledgerYear = ledgerDate.year
            val ledgerMonth = ledgerDate.monthValue

            if (ledgerYear == previousYear && ledgerMonth == previousMonth) {
                if (minusMonth == 1.toLong()) {
                    ledgerList.add(item)
                    sumSalesAmount += item.saleAmount
                    sumCollectAmount += item.collectAmount
                } else {
                    previousBalance += item.saleAmount - item.collectAmount
                }
            }
        }
        return if (minusMonth == 1.toLong()) {
            if (sales) {
                sumSalesAmount
            } else {
                sumCollectAmount
            }
        } else {
            previousBalance
        }
    }
}