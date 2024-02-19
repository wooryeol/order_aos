package kr.co.kimberly.wma.menu.information

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.model.AccountInfoModel
import kr.co.kimberly.wma.model.ProductInfoModel

class InformationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val accountInfoList = ArrayList<AccountInfoModel>()
    private val productInfoList = ArrayList<ProductInfoModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.accountInfo.isChecked = true

        accountInfoList.add(
            AccountInfoModel(
            "000052",
            "파란마트",
            "김파란",
            "123-45-67890",
            "010-1234-5678",
            "서울특별시 마포구 상암동",
            "비즈위즈시스템",
            "-",
            "김우렬",
            "010-63784307") )

        productInfoList.add(
            ProductInfoModel(
                "유한킴벌리(주)",
                "00223",
                "크리넥스 수앤수 20매*5",
                "8801234567890",
                "10",
                "720x10x435 10.9",
                "서울특별시 중구"
            )
        )

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
}