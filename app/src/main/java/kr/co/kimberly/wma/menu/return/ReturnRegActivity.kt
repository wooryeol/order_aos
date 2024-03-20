package kr.co.kimberly.wma.menu.`return`

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.RegAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActReturnRegBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import java.text.DecimalFormat


class ReturnRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActReturnRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var accountName = ""
    private var totalAmount = 0

    private val decimal = DecimalFormat("#,###")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActReturnRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu03)
        mBinding.bottom.bottomButton.text = getString(R.string.orderApproval)

        val returnAdapter = RegAdapter(mContext, mActivity) { items, name ->
            var totalMoney = 0

            items.map {
                val stringWithoutComma = it.totalAmount.replace(",", "")
                totalMoney += stringWithoutComma.toInt()
            }

            accountName = name.ifEmpty {
                accountName
            }
            totalAmount = totalMoney

            val formatTotalMoney = decimal.format(totalMoney).toString()
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"

        }

        mBinding.recyclerview.adapter = returnAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()

                // 주문 승인을 하지 않고 나갈 때 바로 나갈건지 혹은 팝업을 띄워서 리스트는 저장되지 않습니다라는 메세지 보여줄 지 물어보기
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : $accountName\n총금액: ${decimal.format(totalAmount)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                if (returnAdapter.dataList.isEmpty()) {
                    Toast.makeText(mContext, "제품이 등록되지 않았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Log.d("tttt", "취소 클릭함")
                        }

                        override fun onOkClick() {
                            Toast.makeText(v.context, "반품주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext, PrinterOptionActivity::class.java))
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
    }
}