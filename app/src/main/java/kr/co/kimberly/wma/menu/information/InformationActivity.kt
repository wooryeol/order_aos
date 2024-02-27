package kr.co.kimberly.wma.menu.information

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import android.widget.RadioGroup.OnCheckedChangeListener
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupSearchResult
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.model.AccountInfoModel
import kr.co.kimberly.wma.model.ProductInfoModel
import kr.co.kimberly.wma.model.SearchResultModel

class InformationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

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
                    val list = ArrayList<SearchResultModel>()

                    for(i: Int in 1..15) {
                        list.add(SearchResultModel("(I00$i) 기본창고"))
                    }

                    val popupSearchResult = PopupSearchResult(mContext, list)
                    popupSearchResult.onItemSelect = {
                        if(mBinding.accountInfo.isChecked) {
                            getAccountInfo()
                        } else {
                            getProductInfo()
                        }
                    }
                    popupSearchResult.show()
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

    private fun getAccountInfo() {
        val accountInfo = AccountInfoModel(
            "000052",
            "파란마트",
            "김파란",
            "123-45-67890",
            "010-1234-5678",
            "02-123-4567",
            "서울특별시 마포구 상암동",
            "비즈위즈시스템",
            "-",
            "김우렬",
            "010-6378-4307"
        )

        mBinding.accountCode.text = accountInfo.accountCode
        mBinding.account.text = accountInfo.accountName
        mBinding.represent.text = accountInfo.represent
        mBinding.businessNum.text = accountInfo.businessNum
        mBinding.phone.text = accountInfo.phone
        mBinding.phone.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        mBinding.fax.text = accountInfo.fax
        mBinding.address.text = accountInfo.address
        mBinding.customer.text = accountInfo.customer
        mBinding.scale.text = accountInfo.scale
        mBinding.inCharge.text = accountInfo.inCharge
        mBinding.inChargeNum.text = accountInfo.inChargeNum
        mBinding.inChargeNum.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun getProductInfo() {
        val productInfo = ProductInfoModel(
            "유한킴벌리(주)",
            "00223",
            "크리넥스 수앤수 20매*5",
            "8801234567890",
            "10",
            "720x10x435 10.9",
            "서울특별시 중구"
        )

        mBinding.manufacturer.text = productInfo.manufacturer
        mBinding.productCode.text = productInfo.productCode
        mBinding.productName.text = productInfo.productName
        mBinding.barcode.text = productInfo.barcode
        mBinding.incomeQty.text = productInfo.incomeQty
        mBinding.Dimension.text = productInfo.dimension
        mBinding.tax.text = productInfo.tax
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