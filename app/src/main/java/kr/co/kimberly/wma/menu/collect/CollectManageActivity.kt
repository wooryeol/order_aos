package kr.co.kimberly.wma.menu.collect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.CollectListAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActCollectManageBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.CollectModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import retrofit2.Call
import retrofit2.Response

class CollectManageActivity : AppCompatActivity() {

    private lateinit var mBinding: ActCollectManageBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보

    private var collectList: ArrayList<CollectModel>? = null
    private var customerCd : String? = null
    private var adapter : CollectListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectManageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!

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
                    mBinding.accountName.text = it.custNm
                    customerCd = it.custCd
                    searchCollectList()
                }
                popupAccountSearch.show()
            }
        })

        mBinding.btEmpty.setOnClickListener(object: OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                mBinding.accountName.text = getString(R.string.accountHint)
                mBinding.btEmpty.visibility = View.INVISIBLE
                collectList?.clear()
                //showCollectList(collectList!!)
                adapter?.notifyDataSetChanged()
            }
        })

        // 거래처 검색
        mBinding.btSearch.visibility = View.GONE
        mBinding.btSearch.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                searchCollectList()
            }
        })

        // UI 설정
        setUI()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUI() {
        val format = "yyyy/MM/dd"
        mBinding.startDate.text = Utils.getDateFormat(format, Define.MONTH, -1)
        mBinding.endDate.text = Utils.getDateFormat(format, Define.TODAY)

        mBinding.accountName.isSelected = true
    }

    // 검색을 눌렀을 때
    private fun showCollectList(list: ArrayList<CollectModel>) {
        adapter = CollectListAdapter(mContext, mActivity, list)
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (list.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE
        } else {
            mBinding.noSearch.visibility = View.VISIBLE
            mBinding.recyclerview.visibility = View.GONE
        }
    }

    // 거래처 기간별 수금 목록 조회
    fun searchCollectList() {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        /*val agencyCd = mLoginInfo.agencyCd!!
        val userId = mLoginInfo.userId!!
        val searchFromDate = mBinding.startDate.text.toString()
        val searchToDate = mBinding.endDate.text.toString()*/

        // wooryeol
        val agencyCd = "C000028"
        val userId = "mb2004"
        val searchFromDate = "2010/01/01"
        val searchToDate = "2024/05/31"
        customerCd = "001910"

        val call = service.collect(agencyCd, userId, searchFromDate, searchToDate, customerCd!!)
        call.enqueue(object : retrofit2.Callback<ListResultModel<CollectModel>> {
            override fun onResponse(
                call: Call<ListResultModel<CollectModel>>,
                response: Response<ListResultModel<CollectModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        Utils.Log("item search success ====> ${Gson().toJson(item)}")
                        if (item.data.isNullOrEmpty()) {
                            PopupNotice(mContext, "조회 결과가 없습니다.\n다시 검색해주세요.", null).show()
                        } else {
                            collectList = item.data as ArrayList<CollectModel>
                            showCollectList(collectList!!)
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<CollectModel>>, t: Throwable) {
                Utils.Log("item search failed ====> ${t.message}")
            }

        })
    }
}
