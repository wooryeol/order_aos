package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.adapter.AccountSearchAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupAccountSearchBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import retrofit2.Call
import retrofit2.Response

@SuppressLint("NotifyDataSetChanged")
class PopupAccountSearch(mContext: Context): Dialog(mContext) {
    private lateinit var mBinding: PopupAccountSearchBinding
    private var mLoginInfo: LoginResponseModel? = null // 로그인 정보
    private var context = mContext

    var onItemSelect: ((CustomerModel) -> Unit)? = null
    var list : List<CustomerModel>? = null
    var adapter: AccountSearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupAccountSearchBinding.inflate(layoutInflater)
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

        adapter = AccountSearchAdapter(context)
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        mBinding.btLogin.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                if (mBinding.etAccount.text.isNullOrEmpty()) {
                    Toast.makeText(context, "거래처를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    searchCustomer() // 거래처 검색 통신
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(mBinding.etAccount.windowToken, 0)
                }
            }
        })

        mBinding.etAccount.addTextChangedListener {
            if (mBinding.etAccount.text.isNullOrEmpty()) {
                mBinding.btProductNameEmpty.visibility = View.GONE
            } else {
                mBinding.btProductNameEmpty.visibility = View.VISIBLE
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.etAccount.text = null
                adapter?.dataList = emptyList()
                mBinding.noSearch.visibility = View.GONE
                mBinding.recyclerview.visibility = View.VISIBLE
                adapter?.notifyDataSetChanged()
            }
        })

        adapter?.itemClickListener = object: AccountSearchAdapter.ItemClickListener {
            override fun onItemClick(item: CustomerModel) {
                onItemSelect?.invoke(item)
                hideDialog()
            }
        }
    }

    fun hideDialog() {
        if (isShowing) {
            dismiss()
        }
    }
    fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(mBinding.btLogin.windowToken, 0) //hide keyboard
            return true
        }
        return false
    }

    private fun searchCustomer() {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val searchCondition = mBinding.etAccount.text.toString()
        val call = service.client(mLoginInfo?.agencyCd!!, mLoginInfo?.userId!!, searchCondition)

        call.enqueue(object : retrofit2.Callback<ListResultModel<CustomerModel>> {
            override fun onResponse(
                call: Call<ListResultModel<CustomerModel>>,
                response: Response<ListResultModel<CustomerModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        Utils.Log("account search success ====> ${Gson().toJson(item)}")

                        if (item.data.isNullOrEmpty()) {
                            PopupNotice(context, "조회 결과가 없습니다.\n다시 검색해주세요.", null).show()
                        } else {
                            list = item.data
                            adapter?.dataList = list!!
                            adapter?.notifyDataSetChanged()

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

}