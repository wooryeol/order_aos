package kr.co.kimberly.wma.menu.information

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountInformation
import kr.co.kimberly.wma.custom.popup.PopupAccountSearchV2
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ListResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import retrofit2.Call
import retrofit2.Response

class InformationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener

    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private var mCustomerInfo: CustomerModel? = null // 거래처 정보
    private var mItemInfo: SearchItemModel? = null // 제품 정보
    private var mSearchType: String? = null // 조회 유형

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!

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
                    val popupNotice = PopupNotice(mContext, getString(R.string.etSearchEmpty))
                    popupNotice.show()
                } else {
                    val popupAccountInformation = PopupAccountInformation(mContext, )
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

    private fun getInfo(searchCondition: String) {
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        //val call = service.masterInfo(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, mSearchType, searchCondition)
        //test
        val call = service.masterInfo("C000000", "mb2004", "C", "마트")


        call.enqueue(object : retrofit2.Callback<ListResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ListResultModel<DataModel<Unit>>>,
                response: Response<ListResultModel<DataModel<Unit>>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnMsg == Define.SUCCESS) {
                        if (item.data.isNullOrEmpty()) {
                            PopupNotice(mContext, "조회 결과가 없습니다.\n다시 검색해주세요.", null).show()
                        } else {
                            Utils.Log("info search success ====> ${Gson().toJson(item.data)}")

                            if (mSearchType == Define.TYPE_CUSTOMER) {
                                mCustomerInfo = item.data as CustomerModel

                                val popupAccountInformation = PopupAccountInformation(mContext, )
                                popupAccountInformation.onItemSelect = {
                                    setInfo()
                                }
                                popupAccountInformation.show()

                            } else {
                                mItemInfo = item.data as SearchItemModel

                            }
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ListResultModel<DataModel<Unit>>>, t: Throwable) {
                Utils.Log("stock failed ====> ${t.message}")
            }

        })
    }

    private fun setInfo(){
        /*val popupSearchResult = PopupSearchResult(mContext, list)
        popupSearchResult.onItemSelect = {
            when(mSearchType) {
                Define.TYPE_CUSTOMER -> {
                    *//*mBinding.accountCode.text = mCustomerInfo?.custCd
                    mBinding.account.text = mCustomerInfo?.custNm
                    mBinding.represent.text = mCustomerInfo
                    mBinding.businessNum.text = mCustomerInfo?.custCd
                    mBinding.phone.text = mCustomerInfo?.custCd
                    mBinding.phone.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    mBinding.fax.text = mCustomerInfo?.custCd
                    mBinding.address.text = mCustomerInfo?.custCd
                    mBinding.customer.text = mCustomerInfo?.custCd
                    mBinding.scale.text = mCustomerInfo?.custCd
                    mBinding.inCharge.text = mCustomerInfo?.custCd
                    mBinding.inChargeNum.text = mCustomerInfo?.custCd
                    mBinding.inChargeNum.paintFlags = Paint.UNDERLINE_TEXT_FLAG*//*
                }
                Define.TYPE_ITEM -> {
                    *//*mBinding.manufacturer.text = productInfo.manufacturer
                    mBinding.productCode.text = productInfo.productCode
                    mBinding.productName.text = productInfo.productName
                    mBinding.barcode.text = productInfo.barcode
                    mBinding.incomeQty.text = productInfo.incomeQty
                    mBinding.Dimension.text = productInfo.dimension
                    mBinding.tax.text = productInfo.tax*//*
                }
            }
        }
        popupSearchResult.show()*/
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