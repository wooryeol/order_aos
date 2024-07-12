package kr.co.kimberly.wma.menu.slip

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SlipListAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSlipSearch
import kr.co.kimberly.wma.custom.popup.PopupDatePicker02
import kr.co.kimberly.wma.databinding.ActSlipInquiryBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.SlipOrderListModel
import retrofit2.Call
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class SlipInquiryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보
    private val orderSlipList = ArrayList<SlipOrderListModel>() // 주문&반품 전표 조회 리스트
    private var popupSearchResult : PopupAccountSlipSearch ? = null
    var slipAdapter: SlipListAdapter? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()

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

        mBinding.etAccountName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                mBinding.btSearch.performClick()
                true
            } else {
                false
            }
        }
    }
    private fun dateToNumber(selectedDate: String): Int {
        val splitStr = selectedDate.split("/")
        return splitStr.joinToString(separator = "").toInt()
    }

    private fun textClear() {
        mBinding.btAccountNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.etAccountName.text = null
                mBinding.tvAccountName.text = null
                mBinding.tvAccountName.visibility = View.GONE
                mBinding.etAccountName.visibility = View.VISIBLE
                mBinding.etAccountName.hint = v.context.getString(R.string.productNameHint)
                mBinding.noSearch.visibility = View.VISIBLE
                mBinding.recyclerview.visibility = View.GONE
                orderSlipList.clear()
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
        slipAdapter = SlipListAdapter(mContext, mActivity, orderSlipList)
        mBinding.recyclerview.adapter = slipAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
        if (orderSlipList.isNotEmpty()){
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
                    Utils.popupNotice(mContext, "조회하실 날짜를 선택해주세요")
                } else if(mBinding.startDate.text.isNotEmpty() && mBinding.endDate.text.isNotEmpty()){
                    if (dateToNumber(mBinding.startDate.text.toString()) > dateToNumber(mBinding.endDate.text.toString())) {
                        Utils.popupNotice(mContext, "입력한 날짜를 확인해주세요")
                    } else if (mBinding.etAccountName.text.isNullOrEmpty()) {
                        Utils.popupNotice(mContext, "거래처를 입력해주세요")
                    } else {
                        // 거래처 검색
                        searchCustomer()
                    }
                }
            }
        })
    }

    private fun searchCustomer() {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val searchCondition = mBinding.etAccountName.text.toString()
        val call = service.client(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, searchCondition)

        call.enqueue(object : retrofit2.Callback<ListResultModel<CustomerModel>> {
            override fun onResponse(
                call: Call<ListResultModel<CustomerModel>>,
                response: Response<ListResultModel<CustomerModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.Log("account search success ====> ${Gson().toJson(item)}")
                        if (item.data.isNullOrEmpty()) {
                            Utils.popupNotice(mContext, "조회 결과가 없습니다.\n다시 검색해주세요.")
                        } else {
                            val list = item.data as ArrayList<CustomerModel>
                            val searchFromDate = mBinding.startDate.text.toString()
                            val searchToDate = mBinding.endDate.text.toString()
                            popupSearchResult = PopupAccountSlipSearch(mBinding.root.context, list, searchFromDate, searchToDate, mBinding.radioOrder.isChecked)
                            popupSearchResult?.onTitleSelect = {
                                mBinding.tvAccountName.isSelected = true
                                mBinding.tvAccountName.text = it.custNm
                                mBinding.etAccountName.visibility = View.GONE
                                mBinding.tvAccountName.visibility = View.VISIBLE


                                if (!mBinding.tvAccountName.text.isNullOrEmpty()) {
                                    showCollectList()
                                }
                            }

                            popupSearchResult?.onItemSelect = {
                                for(i in it) {
                                    orderSlipList.add(i)
                                }
                                slipAdapter?.notifyDataSetChanged()

                                if (orderSlipList.isNotEmpty()){
                                    mBinding.noSearch.visibility = View.GONE
                                    mBinding.recyclerview.visibility = View.VISIBLE
                                } else {
                                    mBinding.noSearch.visibility = View.VISIBLE
                                    mBinding.recyclerview.visibility = View.GONE
                                }
                            }
                            popupSearchResult?.show()

                            if (list.isNullOrEmpty()) {
                                mBinding.recyclerview.visibility = View.GONE
                                mBinding.noSearch.visibility = View.VISIBLE
                            } else {
                                mBinding.recyclerview.visibility = View.VISIBLE
                                mBinding.noSearch.visibility = View.GONE
                            }
                        }
                    }
                } else {

                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<CustomerModel>>, t: Throwable) {
                Utils.Log("search failed ====> ${t.message}")
            }

        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Define.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra("deletedSlipNo")
            if (!result.isNullOrEmpty()){
                slipAdapter?.dataList?.forEach {
                    if (it.slipNo == result) {
                        slipAdapter?.dataList!!.remove(it)
                    }
                }
                slipAdapter?.notifyDataSetChanged()
            }
        }
    }
}