package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.AccountSearchAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupSearchResultBinding
import kr.co.kimberly.wma.menu.slip.SlipInquiryActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.SlipOrderListModel
import retrofit2.Call
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class PopupAccountListSearch(mContext: Context, private val searchCondition: String): Dialog(mContext) {
    private lateinit var mBinding: PopupSearchResultBinding
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보
    private var context = mContext
    var onItemSelect: ((CustomerModel) -> Unit)? = null
    var adapter: AccountSearchAdapter? = null
    var dataList: ArrayList<CustomerModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchResultBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private fun initViews() {
        mLoginInfo = Utils.getLoginData()

        // setCancelable(false) // 뒤로가기 버튼, 바깥 화면 터치시 닫히지 않게

        // (중요) Dialog 는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius 가 보이지 않음
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        mBinding.title.text = context.getString(R.string.account)

        // 아이템 검색
        searchCustomer(searchCondition)

        adapter = AccountSearchAdapter(context)
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        adapter?.itemClickListener = object: AccountSearchAdapter.ItemClickListener {
            override fun onItemClick(item: CustomerModel) {
                onItemSelect?.invoke(item)
                hideDialog()
            }
        }

        mBinding.btnClose.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                hideDialog()
            }
        })
    }

    fun hideDialog() {
        if (isShowing) {
            dismiss()
        }
    }

    private fun searchCustomer(searchCondition: String) {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
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

                        dataList = item.data as ArrayList<CustomerModel>

                        if(dataList.size > 6) {
                            Utils.dialogResize(context, window)
                        }

                        adapter?.dataList = dataList
                        adapter?.notifyDataSetChanged()

                    } else {
                        Utils.popupNotice(context, item?.returnMsg!!)
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
}