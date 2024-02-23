package kr.co.kimberly.wma.menu.collect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.custom.popup.PopupNoteType
import kr.co.kimberly.wma.databinding.ActCollectManageBinding
import kr.co.kimberly.wma.menu.main.MainActivity
import kr.co.kimberly.wma.model.AccountModel

class CollectManageActivity : AppCompatActivity() {

    private lateinit var mBinding: ActCollectManageBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private val collectList = ArrayList<AccountModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectManageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        // 헤더 설정 변경
        mBinding.header.headerTitle.text = getString(R.string.menu02)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        // 바텀 설정 변경
        mBinding.bottom.bottomButton.text = getString(R.string.collectRegi)

        // 수금등록
        mBinding.bottom.bottomButton.setOnClickListener {
            Utils.moveToPage(mContext, CollectRegiActivity())
        }

        // 날짜 선택
        /*val datePickerDialog = PopupDatePicker(mContext)
        mBinding.startDate.setOnClickListener {
            datePickerDialog.showDatePickerDialog(mBinding.startDate)
        }
        mBinding.endDate.setOnClickListener {
            datePickerDialog.showDatePickerDialog(mBinding.endDate)
        }*/

        // 거래처 검색
        mBinding.accountArea.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mContext)
                popupAccountSearch.onItemSelect = {
                    mBinding.btEmpty.visibility = View.VISIBLE
                    mBinding.accountName.text = it.name
                }
                popupAccountSearch.show()
            }
        })

        mBinding.btEmpty.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.accountName.text = getString(R.string.accountHint)
                mBinding.btEmpty.visibility = View.INVISIBLE
            }
        })

        // 거래처 검색
        mBinding.btSearch.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                showCollectList()
            }
        })

        // UI 설정
        setUI()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUI() {
        val format = "yyyy/MM/dd"
        mBinding.startDate.text = Utils.getDateFormat(format, Define.TODAY)
        mBinding.endDate.text = Utils.getDateFormat(format, Define.MONTH, 1)

        mBinding.accountName.isSelected = true
    }

    // 검색을 눌렀을 때
    private fun showCollectList() {
        collectList.add(AccountModel("202312000131", "(000020) 경주마트", "30,000"))

        val adapter = CollectListAdapter(mContext, mActivity)
        adapter.dataList = collectList
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
