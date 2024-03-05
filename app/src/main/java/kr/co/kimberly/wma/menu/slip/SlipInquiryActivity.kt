package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.custom.popup.PopupDatePicker02
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNotification
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.ActMainBinding
import kr.co.kimberly.wma.databinding.ActSlipInquiryBinding
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.menu.purchase.PurchaseRequestActivity
import kr.co.kimberly.wma.menu.`return`.ReturnRegActivity
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.MainMenuModel
import kr.co.kimberly.wma.model.SearchResultModel
import java.text.SimpleDateFormat
import java.util.Calendar

class SlipInquiryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    var slipAdapter: CollectListAdapter? = null

    companion object {
        val collectList = ArrayList<AccountModel>()
        val list = ArrayList<SearchResultModel>()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        setUi()
        showImageButton()
        searchAccount()
        textClear()

        // 날짜 선택

        mBinding.startDate.setOnClickListener {
            val popupDatePicker = PopupDatePicker02(mContext, isDate = false, isStartDate = true)
            popupDatePicker.onSelectedDate = {
                mBinding.startDate.text = it
            }
            popupDatePicker.show()
        }

        mBinding.endDate.setOnClickListener {
            val popupDatePicker = PopupDatePicker02(mContext, isDate = false, isStartDate = false)
            popupDatePicker.onSelectedDate = {
                mBinding.endDate.text = it
            }
            popupDatePicker.show()
        }
    }
    private fun dateToNumber(selectedDate: String): Int {
        val splitStr = selectedDate.split("/")
        return splitStr.joinToString(separator = "").toInt()
    }

    private fun textClear() {
        mBinding.btAccountNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                mBinding.etAccountName.text = null
                mBinding.tvAccountName.text = null
                mBinding.tvAccountName.visibility = View.GONE
                mBinding.etAccountName.visibility = View.VISIBLE
                mBinding.etAccountName.hint = v.context.getString(R.string.productNameHint)
                mBinding.noSearch.visibility = View.VISIBLE
                mBinding.recyclerview.visibility = View.GONE
                collectList.clear()
                slipAdapter?.notifyDataSetChanged()
            }
        })
    }
    private fun setUi(){
        mBinding.header.headerTitle.text = getString(R.string.menu04)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })
    }

    private fun showCollectList() {
        for (i in 1..15) {
            collectList.add(AccountModel("202312000131", "(000020) 경주마트[$i]", "30,000"))
        }

        slipAdapter = CollectListAdapter(mContext, mActivity)
        slipAdapter?.dataList = collectList
        slipAdapter?.isSlipAct = true // 전표조회에서 진입했다는 걸 알려줌
        mBinding.recyclerview.adapter = slipAdapter
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

    private fun searchAccount(){
        mBinding.btSearch.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if (mBinding.startDate.text.isNullOrEmpty() || mBinding.endDate.text.isNullOrEmpty()) {
                    Toast.makeText(v.context, "조회하실 날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                } else if(mBinding.startDate.text.isNotEmpty() && mBinding.endDate.text.isNotEmpty()){
                    if (dateToNumber(mBinding.startDate.text.toString()) > dateToNumber(mBinding.endDate.text.toString())) {
                        val popupNotice = PopupNotice(mContext, "입력한 날짜를 확인해주세요")
                        popupNotice.show()
                    } else if (mBinding.etAccountName.text.isNullOrEmpty()) {
                        Toast.makeText(v.context, "거래처를 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        list.clear()
                        for(i: Int in 1..15) {
                            list.add(SearchResultModel("(000020) 경주마트[$i]"))
                        }
                        val popupSearchResult = PopupSearchResult(mBinding.root.context, list)
                        popupSearchResult.onItemSelect = {
                            mBinding.tvAccountName.isSelected = true
                            mBinding.tvAccountName.text = it.name
                            mBinding.etAccountName.visibility = View.GONE
                            mBinding.tvAccountName.visibility = View.VISIBLE

                            if (!mBinding.tvAccountName.text.isNullOrEmpty()) {
                                showCollectList()
                            }
                        }
                        popupSearchResult.show()
                    }
                }
            }
        })
    }
}