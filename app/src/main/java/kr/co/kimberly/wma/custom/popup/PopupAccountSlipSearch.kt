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
class PopupAccountSlipSearch(mContext: Context, val dataList: ArrayList<CustomerModel>, private val searchFromDate: String? = null, private val searchToDate: String? = null, val order: Boolean? = null ): Dialog(mContext) {
    private lateinit var mBinding: PopupSearchResultBinding
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보
    private var context = mContext
    var onItemSelect: ((ArrayList<SlipOrderListModel>) -> Unit)? = null
    var onTitleSelect: ((CustomerModel) -> Unit)? = null
    var adapter: AccountSearchAdapter? = null

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

        if(dataList.size > 6) {
            Utils.dialogResize(context, window)
        }

        adapter = AccountSearchAdapter(context)
        adapter?.dataList = dataList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        adapter?.itemClickListener = object: AccountSearchAdapter.ItemClickListener {
            override fun onItemClick(item: CustomerModel) {
                onTitleSelect?.invoke(item)
                searchOrderSlipList(item.custCd)
                hideDialog()
            }
        }

        mBinding.btnClose.setOnClickListener(object : OnSingleClickListener(){
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

    private fun searchOrderSlipList(customerCd: String){
        val agencyCd = mLoginInfo?.agencyCd!!
        val userId = mLoginInfo?.userId!!
        val slipType = if (order!!) Define.ORDER else Define.RETURN

        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.orderSlipList(agencyCd, userId, searchFromDate?.replace("/","-"), searchToDate?.replace("/","-"), customerCd, slipType)
        //test
        //val call = service.orderSlipList("C000028", "mb2004", "2024-06-01", "2024-06-27", "000001", "NN")
        call.enqueue(object : retrofit2.Callback<ListResultModel<SlipOrderListModel>> {
            override fun onResponse(
                call: Call<ListResultModel<SlipOrderListModel>>,
                response: Response<ListResultModel<SlipOrderListModel>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.returnCd == Define.RETURN_CD_00 || data?.returnCd == Define.RETURN_CD_90 || data?.returnCd == Define.RETURN_CD_91) {
                        Utils.Log("OrderSlipList search success ====> ${Gson().toJson(data)}")
                        onItemSelect?.invoke(data.data as ArrayList<SlipOrderListModel>)
                        SlipInquiryActivity().slipAdapter?.notifyDataSetChanged()
                    } else {
                        Utils.popupNotice(context, data?.returnMsg!!)
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<SlipOrderListModel>>, t: Throwable) {
                Utils.Log("search failed ====> ${t.message}")
            }

        })
    }
}