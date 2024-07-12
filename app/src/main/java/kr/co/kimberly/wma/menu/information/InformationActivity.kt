package kr.co.kimberly.wma.menu.information

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountInformation
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.DetailInfoModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import kr.co.kimberly.wma.network.model.SlipOrderListModel
import retrofit2.Call
import retrofit2.Response

class InformationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener

    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private var mSearchType: String? = null // 조회 유형
    private var popupInformation : PopupAccountInformation? = null // 정보조회
    private var detailInfoModel: DetailInfoModel? = null // 상세정보

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!
        mSearchType = Define.TYPE_CUSTOMER


        setSetting()

        //헤더 설정
        mBinding.header.headerTitle.text = getString(R.string.menu09)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.radioGroup.setOnCheckedChangeListener(radioGroupCheckedListener)

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.etSearch.text.isEmpty()) {
                    Utils.popupNotice(mContext, getString(R.string.etSearchEmpty))
                } else {
                    getInfo(mBinding.etSearch.text.toString())
                }
            }
        })

        mBinding.phone.setOnClickListener {
            if(mBinding.phone.text.isNotEmpty()) {
                checkPermission(mBinding.phone.text.toString())
            }
        }

        mBinding.inChargeNum.setOnClickListener {
            if(mBinding.inChargeNum.text.isNotEmpty()) {
                checkPermission(mBinding.inChargeNum.text.toString())
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                clearButton()
            }
        })

        mBinding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                mBinding.search.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun setSetting() {
        radioGroupCheckedListener = OnCheckedChangeListener { group, checkedId ->
            hideKeyboard()

            when(checkedId) {
                R.id.accountInfo -> {
                    mBinding.etSearch.hint = getString(R.string.accountHint)
                    mBinding.accountInfoLayout.visibility = View.VISIBLE
                    mBinding.productInfoLayout.visibility = View.GONE
                    mSearchType = Define.TYPE_CUSTOMER

                    // 데이터 초기화
                    mBinding.accountCode.text = ""
                    mBinding.account.text = ""
                    mBinding.represent.text = ""
                    mBinding.businessNum.text = ""
                    mBinding.phone.text = ""
                    mBinding.fax.text = ""
                    mBinding.address.text = ""
                    mBinding.customer.text = ""
                    mBinding.scale.text = ""
                    mBinding.inCharge.text = ""
                    mBinding.inChargeNum.text = ""
                }
                R.id.productInfo -> {
                    mBinding.etSearch.hint = getString(R.string.productHint)
                    mBinding.productInfoLayout.visibility = View.VISIBLE
                    mBinding.accountInfoLayout.visibility = View.GONE
                    mSearchType = Define.TYPE_ITEM

                    mBinding.manufacturer.text = ""
                    mBinding.productCode.text = ""
                    mBinding.productName.text = ""
                    mBinding.barcode.text = ""
                    mBinding.incomeQty.text = ""
                    mBinding.Dimension.text = ""
                    mBinding.tax.text = ""
                }
            }
        }
    }

    fun clearButton() {
        mBinding.etSearch.text = null
        when(mSearchType){
            Define.TYPE_CUSTOMER -> {
                mBinding.etSearch.hint = mContext.getString(R.string.accountHint)
            }
            Define.TYPE_ITEM -> {
                mBinding.etSearch.hint = mContext.getString(R.string.productNameHint)
            }
        }
        mBinding.etSearch.visibility = View.VISIBLE
        mBinding.tvProductName.text = null
        mBinding.tvProductName.visibility = View.GONE
        mBinding.btProductNameEmpty.visibility = View.GONE
    }

    private fun getInfo(searchCondition: String) {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.masterInfo(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, mSearchType!!, searchCondition)
        //test
        //val call = service.masterInfo("C000000", "mb2004", mSearchType!!, searchCondition)


        call.enqueue(object : retrofit2.Callback<ObjectResultModel<DataModel<Any>>> {
            override fun onResponse(
                call: Call<ObjectResultModel<DataModel<Any>>>,
                response: Response<ObjectResultModel<DataModel<Any>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        Utils.Log("item ====> $item")
                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            val gson = Gson()
                            when(mSearchType) {
                                Define.TYPE_CUSTOMER -> {
                                    Utils.Log("customer info search success ====> ${Gson().toJson(item.data?.customerList)}")

                                    // customerList를 JSON 문자열로 변환 후 다시 List<Customer>로 변환
                                    val jsonElement = gson.toJsonTree(item.data?.customerList)
                                    val jsonString = gson.toJson(jsonElement)
                                    val customerListType = object : TypeToken<ArrayList<SlipOrderListModel>>() {}.type
                                    val customerList: ArrayList<SlipOrderListModel> = gson.fromJson(jsonString, customerListType)

                                    popupInformation = PopupAccountInformation(mContext, customerList, null  )
                                    popupInformation?.onAccountSelect = {
                                        mBinding.tvProductName.text = it.customerNm
                                        getDetailInfo(it.customerCd.toString())
                                    }
                                    popupInformation?.show()
                                }

                                Define.TYPE_ITEM -> {
                                    Utils.Log("item info search success ====> ${Gson().toJson(item.data?.itemList)}")
                                    // itemList를 JSON 문자열로 변환 후 다시 List<Customer>로 변환
                                    val jsonElement = gson.toJsonTree(item.data?.itemList)
                                    val jsonString = gson.toJson(jsonElement)
                                    val itemListType = object : TypeToken<ArrayList<SearchItemModel>>() {}.type
                                    val itemList: ArrayList<SearchItemModel> = gson.fromJson(jsonString, itemListType)

                                    val popupAccountInformation = PopupAccountInformation(mContext, null, itemList)
                                    popupAccountInformation.onItemSelect = {
                                        mBinding.tvProductName.text = it.itemNm
                                        getDetailInfo(it.itemCd.toString())
                                    }
                                    popupAccountInformation.show()
                                }

                                else -> {
                                    Utils.popupNotice(mContext, item.returnMsg)
                                }
                            }
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg)
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<DataModel<Any>>>, t: Throwable) {
                Utils.Log("getInfo failed ====> ${t.message}")
            }

        })
    }


    // 상세 정보 조회
    private fun getDetailInfo(searchCd: String) {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.masterInfoDetail(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, mSearchType!!, searchCd)
        //test
        //val call = service.masterInfoDetail("C000000", "mb2004", mSearchType!!, searchCd)

        //val call = service.masterInfoDetail("C000028", "mb2004", "C", "000012")

        call.enqueue(object : retrofit2.Callback<ObjectResultModel<DetailInfoModel>> {
            override fun onResponse(
                call: Call<ObjectResultModel<DetailInfoModel>>,
                response: Response<ObjectResultModel<DetailInfoModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        Utils.Log("item ====> $item")
                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            val data = item.data
                            when(mSearchType) {
                                Define.TYPE_CUSTOMER -> {
                                    Utils.Log("customer detail info search success ====> ${Gson().toJson(item.data)}")

                                    detailInfoModel = item.data?.copy(
                                        searchType = data?.searchType!!,
                                        customerCd = data.customerCd!!,
                                        customerNm = data.customerNm!!,
                                        representNm = data.representNm!!,
                                        bizNo = data.bizNo!!,
                                        telNo = data.telNo!!,
                                        faxNo = data.faxNo!!,
                                        address = data.address!!,
                                        billingVendor = data.billingVendor!!,
                                        storeSize = data.storeSize!!,
                                        buyEmpNm = data.buyEmpNm!!,
                                        buyEmpMobileNo = data.buyEmpMobileNo!!
                                    )

                                    setInfo(detailInfoModel!!)

                                }

                                Define.TYPE_ITEM -> {
                                    Utils.Log("item detail info search success ====> ${Gson().toJson(item.data)}")

                                    detailInfoModel = item.data?.copy(
                                        representNm =  data?.resultType!!,
                                        makerNm = data.makerNm!!,
                                        itemCd = data.itemCd!!,
                                        itemNm = data.itemNm!!,
                                        kanCode = data.kanCode!!,
                                        dimension = data.dimension!!,
                                        getBoxQty = data.getBoxQty!!,
                                        vatType = data.vatType!!,
                                        registerImgYn = data.registerImgYn!!,
                                        imgUrl = data.imgUrl!!,
                                    )

                                    setInfo(detailInfoModel!!)

                                }

                                else -> {
                                    Utils.popupNotice(mContext, item.returnMsg)
                                }
                            }
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg)
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<DetailInfoModel>>, t: Throwable) {
                Utils.Log("getInfo failed ====> ${t.message}")
            }

        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setInfo(detailInfoModel: DetailInfoModel){
        mBinding.etSearch.visibility = View.GONE
        mBinding.tvProductName.visibility = View.VISIBLE
        mBinding.btProductNameEmpty.visibility = View.VISIBLE

        when(mSearchType) {
            Define.TYPE_CUSTOMER -> {
                mBinding.accountCode.text = detailInfoModel.customerCd
                mBinding.account.text = detailInfoModel.customerNm
                mBinding.represent.text = detailInfoModel.representNm
                mBinding.businessNum.text = detailInfoModel.bizNo
                mBinding.phone.text = detailInfoModel.telNo
                if (detailInfoModel.telNo != "-"){
                    mBinding.phone.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
                mBinding.fax.text = detailInfoModel.faxNo
                mBinding.address.text = detailInfoModel.address
                mBinding.customer.text = detailInfoModel.billingVendor
                mBinding.scale.text = detailInfoModel.storeSize
                mBinding.inCharge.text = detailInfoModel.buyEmpNm
                mBinding.inChargeNum.text = detailInfoModel.buyEmpMobileNo
                if (detailInfoModel.buyEmpMobileNo != "-"){
                    mBinding.inChargeNum.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
            }
            Define.TYPE_ITEM -> {
                mBinding.manufacturer.text = detailInfoModel.makerNm
                mBinding.productCode.text = detailInfoModel.itemCd
                mBinding.productName.text = detailInfoModel.itemNm
                mBinding.barcode.text = detailInfoModel.kanCode
                mBinding.incomeQty.text = Utils.decimal(detailInfoModel.getBoxQty!!)
                mBinding.Dimension.text = detailInfoModel.dimension
                mBinding.Dimension.isSelected = true
                mBinding.tax.text = detailInfoModel.vatType
                if (!detailInfoModel.imgUrl.isNullOrEmpty()){
                    val defaultImage = mContext.getDrawable(R.drawable.imagesmode)
                    Glide.with(this)
                        .load(detailInfoModel.imgUrl) // 불러올 이미지 url
                        .placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                        .error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                        .fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                        //.circleCrop() // 동그랗게 자르기
                        .into(mBinding.imageView) // 이미지를 넣을 뷰
                }
            }
        }
    }

    private fun hideKeyboard() {
        mBinding.etSearch.setText("")
        mBinding.etSearch.clearFocus()

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mBinding.etSearch.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun checkPermission(number: String){
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    call(number)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            })
            .setDeniedMessage("권한을 허용해주세요.\n[설정] > [애플리케이션] > [앱 권한]")
            .setPermissions(Manifest.permission.CALL_PHONE)
            .check()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun call(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${number}")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}