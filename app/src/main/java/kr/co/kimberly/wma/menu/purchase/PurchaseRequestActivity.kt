package kr.co.kimberly.wma.menu.purchase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PurchaseRequestAdapter
import kr.co.kimberly.wma.adapter.RegAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupMessage
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.databinding.ActPrinterOptionBinding
import kr.co.kimberly.wma.databinding.ActPurchaseRequestBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.model.OrderRegModel

class PurchaseRequestActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseRequestBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseRequestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu08)
        mBinding.bottom.bottomButton.text = getString(R.string.menu08)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupMessage = PopupMessage(mContext, "주문 전송", "거래처 : (000018) 신림마트 [0원]\n총금액: 9,999,999원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")

                popupMessage.itemClickListener = object: PopupMessage.ItemClickListener {
                    override fun onCancelClick() {
                        Log.d("tttt", "취소 클릭함")
                    }

                    override fun onOkClick() {
                        startActivity(Intent(mContext, PrinterOptionActivity::class.java))
                    }
                }

                popupMessage.show()
            }
        })

        val list = ArrayList<OrderRegModel>()
        for(i: Int in 1..10) {
            list.add(OrderRegModel("(34870) 하기스프리미어 3공 100/1", "10", "0", "9,999,999원", "240", "9,999,999,999원"))
        }

        val adapter = PurchaseRequestAdapter(mContext, mActivity)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}