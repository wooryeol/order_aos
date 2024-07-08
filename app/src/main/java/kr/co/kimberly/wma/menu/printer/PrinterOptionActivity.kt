package kr.co.kimberly.wma.menu.printer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tscdll.TSCActivity
import com.google.gson.Gson
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
import kr.co.kimberly.wma.databinding.ActPrinterOptionBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.model.OrderRegModel
import kr.co.kimberly.wma.network.model.SalesInfoModel

class PrinterOptionActivity : AppCompatActivity() {
    private lateinit var mBinding: ActPrinterOptionBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mList: ArrayList<OrderRegModel>
    private lateinit var slipNo: String
    private lateinit var title: String

    private var mReceipt: ArrayList<SalesInfoModel>? = null // 최종 아이템 데이터

    private val tscDll = TSCActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPrinterOptionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.scanBtn.visibility = View.GONE

        mReceipt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("data") as? ArrayList<SalesInfoModel>
        }else{
            intent.getSerializableExtra("data") as? ArrayList<SalesInfoModel>
        }
        slipNo = intent.getStringExtra("slipNo")!!
        title = intent.getStringExtra("title")!!

        Utils.Log("PrinterOptionActivity title ====> $title")
        Utils.Log("PrinterOptionActivity slipNo ====> $slipNo")
        Utils.Log("PrinterOptionActivity mReceipt ====> ${Gson().toJson(mReceipt)}")

        mBinding.header.headerTitle.text = title

        //mList = SlipInquiryDetailActivity.list

        mBinding.header.backBtn.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupNoticeV2 = PopupNoticeV2(mContext, "인쇄를 종료하고\n처음 화면으로 돌아가시겠습니까?", object : Handler(
                    Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        when(msg.what) {
                            Define.OK -> {
                                finish()
                            }
                        }
                    }
                })
                popupNoticeV2.show()
            }
        })

        // 인쇄
        mBinding.printBtn.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if (!SettingActivity.checkPrinter) {
                    popupNotice("환경설정에서 프린터 사용여부를 확인 주세요.")
                } else if (mBinding.printQuantity.text.isNullOrEmpty()) {
                    Toast.makeText(mContext, "인쇄 수량을 적어주세요.", Toast.LENGTH_SHORT).show()
                    connectPrinter()
                } else {
                    /*val receiptNumber = "TEXT 30, 10, \"K.BF2\", 0, 0, 1, \"전표 번호: ${mReceipt}\"\r\n"
                    val account = "TEXT 30, 50, \"K.BF2\", 0, 0, 1, \"거래처: ${mReceipt.account}\"\r\n"
                    val total = "TEXT 30, 90, \"K.BF2\", 0, 0, 1, \"총금액: ${mReceipt.totalAmount}원\"\r\n"
                    val box = "BOX 30, 130, 550, 710, 2\r\n"

                    tscDll.sendcommand("GAP 3 mm, 0 mm\r\n")
                    tscDll.sendcommand("DIRECTION 0\r\n")
                    tscDll.sendcommand("SET TEAR OFF\r\n")
                    tscDll.sendcommand("SIZE 75 mm, 100 mm\r\n")
                    tscDll.sendcommand("CLS\r\n")

                    val buffer = StringBuffer()
                    buffer.apply {
                        append(receiptNumber)
                        append(account)
                        append(total)
                        append(box)
                    }

                    tscDll.sendcommand(buffer.toString().toByteArray(Charset.forName("EUC-KR")))
                    tscDll.sendcommand("PRINT ${mBinding.printQuantity.text}, 1\r\n")

                    val dlg = PopupPrintDone(this@PrinterOptionActivity, mActivity)
                    dlg.show()*/
                }
            }
        })
    }

    @SuppressLint("ResourceAsColor")
    fun connectPrinter(){
        val address = SharedData.getSharedData(mContext, SharedData.PRINTER_ADDR, "")
        Utils.Log("address ====> $address")

        if(address.isNotEmpty()) {
            tscDll.openport(address)
            try {
                tscDll.status()
                mBinding.printBtn.isSelected = true
            } catch (e: NullPointerException){
                mBinding.header.scanBtn.setImageResource(R.drawable.print)
                Utils.Log("error ====> $e")
            }
        }else{
            popupNotice("환경설정에서 프린터를 연결해 주세요.")
        }
    }

    private fun popupNotice(msg: String) {
        val popupNotice = PopupNotice(mContext, msg, true)
        popupNotice.show()
    }
}