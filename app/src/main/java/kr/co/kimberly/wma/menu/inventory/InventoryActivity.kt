package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupWarehouseList
import kr.co.kimberly.wma.databinding.ActInventoryBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.WarehouseListModel
import kr.co.kimberly.wma.network.model.WarehouseStockModel
import retrofit2.Call
import retrofit2.Response

class InventoryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInventoryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private lateinit var agencyCd : String // 대리점 코드
    private lateinit var userId : String // 사용자 아이디
    private var warehouseCd: String? = null // 창고 코드
    private var itemList: ArrayList<WarehouseStockModel>? = null
    private var adapter : InventoryListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInventoryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()
        agencyCd =  mLoginInfo.agencyCd!!
        userId = mLoginInfo.userId!!

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

        mBinding.etProductName.addTextChangedListener {
            if (mBinding.etProductName.text.isNullOrEmpty()) {
                mBinding.btProductNameEmpty.visibility = View.GONE
            } else {
                mBinding.btProductNameEmpty.visibility = View.VISIBLE
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.btProductNameEmpty.visibility = View.GONE
                mBinding.tvProductName.text = null
                mBinding.tvProductName.visibility = View.GONE
                mBinding.etProductName.text = null
                mBinding.etProductName.visibility = View.VISIBLE

            }

        })

        mBinding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                mBinding.search.performClick()
                true
            } else {
                false
            }
        }

        // 아이템 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.etProductName.text.toString().isEmpty()) {
                    Utils.popupNotice(mContext, getString(R.string.productNameHint))
                } else {
                    warehouseStock(mBinding.etProductName.text.toString())
                }
            }
        })

        // 창고 선택
        mBinding.tvBranchHouse.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                warehouseList()
            }
        })

        // 제품 삭제
        mBinding.btProductNameEmpty.setOnClickListener(object :OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                mBinding.etProductName.text = null
                mBinding.tvProductName.text = null
                mBinding.tvProductName.visibility = View.GONE
                mBinding.etProductName.visibility = View.VISIBLE
                mBinding.btProductNameEmpty.visibility = View.GONE
                mBinding.etProductName.hint = v.context.getString(R.string.productNameHint)
                mBinding.noSearch.visibility = View.VISIBLE
                itemList?.clear()
                adapter?.notifyDataSetChanged()

            }
        })
    }

    private fun setSetting() {
        // 텍스트를 흘러가게 하기 위함
        mBinding.tvBranchHouse.isSelected = true

        // 진입 시 창고 리스트 팝업 노출
        warehouseList()
    }

    // 검색을 눌렀을 때
    private fun showInventoryList(list: ArrayList<WarehouseStockModel>) {
        adapter = InventoryListAdapter(mContext, mActivity)
        adapter!!.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (list.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(mBinding.etProductName.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            mBinding.etProductName.clearFocus()
        } else {
            Utils.popupNotice(mContext, getString(R.string.searchNothing))
        }
    }

    private fun warehouseList(){
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.warehouseList(agencyCd, userId)

        //test
        //val call = service.warehouseList("C000028", "mb2004")

        call.enqueue(object : retrofit2.Callback<ResultModel<List<WarehouseListModel>>> {
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ResultModel<List<WarehouseListModel>>>,
                response: Response<ResultModel<List<WarehouseListModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("warehouse search success ====> ${Gson().toJson(item)}")
                        val list = item.data as ArrayList<WarehouseListModel>
                        val popupWarehouseList = PopupWarehouseList(mContext, list)
                        popupWarehouseList.onItemSelect = {
                            warehouseCd = it.warehouseCd
                            mBinding.tvBranchHouse.text = "(${it.warehouseCd}) ${it.warehouseNm}"

                            if (!itemList.isNullOrEmpty()) {
                                mBinding.etProductName.text = null
                                mBinding.tvProductName.text = null
                                mBinding.tvProductName.visibility = View.GONE
                                mBinding.etProductName.visibility = View.VISIBLE
                                mBinding.btProductNameEmpty.visibility = View.GONE
                                mBinding.etProductName.hint = mContext.getString(R.string.productNameHint)
                                mBinding.noSearch.visibility = View.VISIBLE
                                itemList?.clear()
                                adapter?.notifyDataSetChanged()
                            }
                        }
                        popupWarehouseList.show()
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<List<WarehouseListModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("warehouse search failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    // 검색 아이템 리스트 조회
    fun warehouseStock(searchCondition: String) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.warehouseStock(agencyCd, userId, warehouseCd!!, searchCondition)
        //test
        //val call = service.warehouseStock("C000028", "mb2004", "I001", "하기스")

        call.enqueue(object : retrofit2.Callback<ResultModel<List<WarehouseStockModel>>> {
            override fun onResponse(
                call: Call<ResultModel<List<WarehouseStockModel>>>,
                response: Response<ResultModel<List<WarehouseStockModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("stock search success ====> ${Gson().toJson(item.data)}")
                        itemList = item.data as ArrayList<WarehouseStockModel>
                        showInventoryList(itemList!!)

                        mBinding.etProductName.visibility = View.GONE
                        mBinding.tvProductName.text = searchCondition
                        mBinding.tvProductName.visibility = View.VISIBLE
                        mBinding.btProductNameEmpty.visibility = View.VISIBLE
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<List<WarehouseStockModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("stock failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }
}