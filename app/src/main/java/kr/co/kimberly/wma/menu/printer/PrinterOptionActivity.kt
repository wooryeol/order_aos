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
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
import kr.co.kimberly.wma.databinding.ActPrinterOptionBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.DetailInfoModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.OrderRegModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import kr.co.kimberly.wma.network.model.SlipPrintModel
import kr.co.kimberly.wma.network.model.WarehouseStockModel
import retrofit2.Call
import retrofit2.Response

class PrinterOptionActivity : AppCompatActivity() {
    private lateinit var mBinding: ActPrinterOptionBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel
    private lateinit var slipNo: String
    private lateinit var moneySlipNo: String
    private lateinit var title: String

    private val tscDll = TSCActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPrinterOptionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()

        mBinding.header.scanBtn.visibility = View.GONE

        slipNo = intent.getStringExtra("slipNo") ?: ""
        title = intent.getStringExtra("title") ?: ""
        moneySlipNo = intent.getStringExtra("moneySlipNo") ?: ""

        Utils.log("PrinterOptionActivity title ====> $title")
        Utils.log("PrinterOptionActivity slipNo ====> $slipNo")

        mBinding.header.headerTitle.text = title

        mBinding.header.backBtn.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupNoticeV2 = PopupNoticeV2(mContext, "인쇄를 종료하고\n처음 화면으로 돌아가시겠습니까?", object : Handler(
                    Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        when(msg.what) {
                            Define.EVENT_OK -> {
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
                if (moneySlipNo != "") {
                    moneySlipPrint(moneySlipNo)
                } else {
                    orderSlipPrint(slipNo)
                }
                /*if (!SettingActivity.checkPrinter) {
                    popupNotice("환경설정에서 프린터 사용여부를 확인 주세요.")
                } else if (mBinding.printQuantity.text.isNullOrEmpty()) {
                    Utils.popupNotice(mContext, "인쇄 수량을 적어주세요.")
                    //connectPrinter()
                } else {
                    *//*val receiptNumber = "TEXT 30, 10, \"K.BF2\", 0, 0, 1, \"전표 번호: ${mReceipt}\"\r\n"
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
                    dlg.show()*//*
                }*/
            }
        })
    }

    @SuppressLint("ResourceAsColor")
    fun connectPrinter(){
        val address = SharedData.getSharedData(mContext, SharedData.PRINTER_ADDR, "")
        Utils.log("address ====> $address")

        if(address.isNotEmpty()) {
            tscDll.openport(address)
            try {
                tscDll.status()
                mBinding.printBtn.isSelected = true
            } catch (e: NullPointerException){
                mBinding.header.scanBtn.setImageResource(R.drawable.print)
                Utils.log("error ====> $e")
            }
        }else{
            popupNotice("환경설정에서 프린터를 연결해 주세요.")
        }
    }

    private fun popupNotice(msg: String) {
        val popupNotice = PopupNotice(mContext, msg, true)
        popupNotice.show()
    }

    // 주문&반품 출력
    fun orderSlipPrint(slipNo: String) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.getOrderSlipPrint(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, slipNo)
        //test
        //val call = service.getOrderSlipPrint("C000028", "mb2004", "20240700053")

        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<DetailInfoModel>>> {
            override fun onResponse(
                call: Call<ResultModel<DataModel<DetailInfoModel>>>,
                response: Response<ResultModel<DataModel<DetailInfoModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("order slip print success ====> ${Gson().toJson(item.data)}")

                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DataModel<DetailInfoModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("order slip print failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }
        })
    }

    // 수금 전표 출력
    fun moneySlipPrint(moneySlipNo: String) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.getMoneySlipPrint(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, moneySlipNo)
        //test
        //val call = service.getMoneySlipPrint("C000028", "mb2004", "20240700003")

        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<SlipPrintModel>>> {
            override fun onResponse(
                call: Call<ResultModel<DataModel<SlipPrintModel>>>,
                response: Response<ResultModel<DataModel<SlipPrintModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("money slip print success ====> ${Gson().toJson(item.data)}")

                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }
            override fun onFailure(call: Call<ResultModel<DataModel<SlipPrintModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("money slip print failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }
        })
    }
}