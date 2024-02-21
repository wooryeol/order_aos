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
import android.view.View
import android.widget.RadioButton
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.model.AccountInfoModel
import kr.co.kimberly.wma.model.ProductInfoModel

class InformationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var accountInfo : AccountInfoModel
    private lateinit var productInfo : ProductInfoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.accountInfo.isChecked = true

        //헤더 설정
        mBinding.header.headerTitle.text = getString(R.string.menu09)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mContext)
                popupAccountSearch.onItemSelect = {
                    mBinding.accountName.text = it.name
                }
                popupAccountSearch.show()
            }
        })

        getProductInfo()
        getAccountInfo()

        if (mBinding.phone.text.isNotEmpty()){
            mBinding.phone.setOnClickListener {
                checkPermission(mBinding.phone.text.toString())
            }
        }

        if (mBinding.inChargeNum.text.isNotEmpty()){
            mBinding.inChargeNum.setOnClickListener {
                checkPermission(mBinding.inChargeNum.text.toString())
            }
        }
    }

    fun onInformationActRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                R.id.accountInfo -> {
                    if (checked) {
                        mBinding.accountInfoLayout.visibility = View.VISIBLE
                        mBinding.productInfoLayout.visibility = View.GONE
                    }
                }
                R.id.productInfo -> {
                    if (checked) {
                        mBinding.productInfoLayout.visibility = View.VISIBLE
                        mBinding.accountInfoLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getAccountInfo() {
        accountInfo = AccountInfoModel(
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
        productInfo = ProductInfoModel(
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