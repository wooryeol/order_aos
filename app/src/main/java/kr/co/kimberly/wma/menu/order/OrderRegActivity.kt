package kr.co.kimberly.wma.menu.order

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
import kr.co.kimberly.wma.custom.TotalValueListener
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.menu.`return`.ReturnRegActivity
import kr.co.kimberly.wma.model.OrderRegModel
import java.text.DecimalFormat


class OrderRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    val decimal = DecimalFormat("#,###")
    private var totalAmount = 0

    companion object {
        val list = ArrayList<OrderRegModel>()
        @SuppressLint("StaticFieldLeak")
        var orderAdapter: RegAdapter? = null
        var accountName = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu01)
        mBinding.bottom.bottomButton.text = getString(R.string.orderApproval)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                list.clear()
                ReturnRegActivity.list.clear()
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : $accountName\n총금액: ${decimal.format(totalAmount)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                if (list.isEmpty()) {
                    Toast.makeText(mContext, "제품이 등록되지 않았습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Log.d("tttt", "취소 클릭함")
                        }

                        override fun onOkClick() {
                            list.clear()
                            ReturnRegActivity.list.clear()
                            Toast.makeText(v.context, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(mContext, PrinterOptionActivity::class.java))
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })
        orderAdapter = RegAdapter(mContext, mActivity, object : TotalValueListener {
            @SuppressLint("SetTextI18n")
            override fun onTotalValueChanged(totalValue: Int) {
                mBinding.tvTotalAmount.text = "${decimal.format(totalValue)}원"
                totalAmount = totalValue
            }
        })

        orderAdapter?.dataList = list
        mBinding.recyclerview.adapter = orderAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}