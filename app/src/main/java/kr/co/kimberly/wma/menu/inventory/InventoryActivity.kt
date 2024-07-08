package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupWarehouseList
import kr.co.kimberly.wma.databinding.ActInventoryBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
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
        mLoginInfo = Utils.getLoginData()!!
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

        // 아이템 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.etProductName.text.toString().isEmpty()) {
                    showNotice(getString(R.string.accountHint))
                } else {
                    warehouseStock(mBinding.etProductName.text.toString())
                }
            }
        })

        // 창고 선택
        mBinding.tvBranchHouse.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                warehouseList()
                /*if (!mBinding.tvBranchHouse.text.isNullOrEmpty()) {

                }*/
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
            showNotice(getString(R.string.searchNothing))
        }
    }

    private fun showNotice(msg: String) {
        val popupNotice = PopupNotice(mContext, msg)
        popupNotice.show()
    }

    private fun warehouseList(){
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        //val call = service.warehouseList(agencyCd, userId)

        //test
        val call = service.warehouseList("C000028", "mb2004")

        call.enqueue(object : retrofit2.Callback<ListResultModel<WarehouseListModel>> {
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ListResultModel<WarehouseListModel>>,
                response: Response<ListResultModel<WarehouseListModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        Utils.Log("warehouse search success ====> ${Gson().toJson(item)}")
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
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<WarehouseListModel>>, t: Throwable) {
                Utils.Log("warehouse search failed ====> ${t.message}")
            }

        })
    }

    // 검색 아이템 리스트 조회
    fun warehouseStock(searchCondition: String) {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        //val call = service.warehouseStock(agencyCd, userId, warehouseCd!!, searchCondition)
        //test
        val call = service.warehouseStock("C000028", "mb2004", "I001", "하기스")

        call.enqueue(object : retrofit2.Callback<ListResultModel<WarehouseStockModel>> {
            override fun onResponse(
                call: Call<ListResultModel<WarehouseStockModel>>,
                response: Response<ListResultModel<WarehouseStockModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        if (item.data.isNullOrEmpty()) {
                            PopupNotice(mContext, "조회 결과가 없습니다.\n다시 검색해주세요.", null).show()
                        } else {
                            Utils.Log("stock search success ====> ${Gson().toJson(item.data)}")
                            itemList = item.data as ArrayList<WarehouseStockModel>
                            showInventoryList(itemList!!)

                            mBinding.etProductName.visibility = View.GONE
                            mBinding.tvProductName.text = searchCondition
                            mBinding.tvProductName.visibility = View.VISIBLE
                            mBinding.btProductNameEmpty.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<WarehouseStockModel>>, t: Throwable) {
                Utils.Log("stock failed ====> ${t.message}")
            }

        })
    }
}