package kr.co.kimberly.wma.menu.slip

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
import kr.co.kimberly.wma.adapter.SlipInquiryDetailAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.TotalValueListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.databinding.ActSlipInquiryDetailBinding
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.menu.`return`.ReturnRegActivity
import kr.co.kimberly.wma.model.AccountModel
import kr.co.kimberly.wma.model.OrderRegModel
import java.text.DecimalFormat

class SlipInquiryDetailActivity : AppCompatActivity() {
    private lateinit var mBinding: ActSlipInquiryDetailBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var receiptNumber = ""
    private var totalMoney = 0

    private val decimal = DecimalFormat("#,###")

    companion object {
        val list = ArrayList<OrderRegModel>()
        var accountName = ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActSlipInquiryDetailBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        for(i: Int in 1..2) {
            list.add(
                OrderRegModel(
                    "(01293) 크리넥스 순수 3겹 티슈 30-2롤(NEW)",
                    "10",
                    "9",
                    "2,000",
                    "249",
                    "498,000"
                )
            )
        }

        showList()
        setUi()
        moveToPage()
        onClickPrint()
        deleteSlip()
    }

    private fun deleteSlip() {
        mBinding.delete.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문전표삭제", "주문번호: ${mBinding.receiptNumber.text}", "선택한 전표가 전표 리스트에서 삭제됩니다.\n삭제하시겠습니까?")
                popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                    override fun onCancelClick() {
                        Log.d("tttt", "취소 클릭함")
                    }

                    override fun onOkClick() {
                        Toast.makeText(v.context, "전표가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                popupDoubleMessage.show()
            }

        })
    }

    private fun onClickPrint() {
        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {

            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "주문 전송", "거래처 : ${accountName}\n총금액: ${decimal.format(totalMoney)}원", "위와 같이 승인을 요청합니다.\n주문전표 전송을 하시겠습니까?")
                popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                    override fun onCancelClick() {
                        Log.d("tttt", "취소 클릭함")
                    }

                    override fun onOkClick() {
                        Toast.makeText(v.context, "주문이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(mContext, PrinterOptionActivity::class.java))
                    }
                }
                popupDoubleMessage.show()
            }
        })
    }
    private fun moveToPage() {
        mBinding.modify.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val intent = Intent(mContext, SlipInquiryModifyActivity::class.java)
                val formatTotalMoney = decimal.format(totalMoney).toString()
                intent.putExtra("totalMoney", formatTotalMoney)
                startActivity(intent)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setUi() {
        mBinding.header.headerTitle.text = getString(R.string.menu04)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.bottom.bottomButton.text = getString(R.string.slipPrint)

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                /*SlipInquiryDetailAdapter.totalValue = 0*/
                finish()
            }
        })

        receiptNumber = intent.getStringExtra("receiptNumber").toString()
        accountName = intent.getStringExtra("account").toString()

        mBinding.receiptNumber.text = receiptNumber
        mBinding.accountName.text = accountName
    }

    @SuppressLint("SetTextI18n")
    private fun showList() {
        list.map {
            val stringWithoutComma = it.totalAmount.replace(",", "")
            totalMoney += stringWithoutComma.toInt()
        }

        val formatTotalMoney = decimal.format(totalMoney).toString()
        mBinding.tvTotalAmount.text = "${formatTotalMoney}원"

        val adapter = SlipInquiryDetailAdapter(mContext) { _, _ -> }

        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}