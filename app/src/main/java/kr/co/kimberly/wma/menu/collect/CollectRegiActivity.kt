package kr.co.kimberly.wma.menu.collect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountListSearch
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNoteType
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
import kr.co.kimberly.wma.custom.popup.PopupOrderSend
import kr.co.kimberly.wma.databinding.ActCollectRegiBinding
import kr.co.kimberly.wma.menu.printer.PrinterOptionActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.BalanceModel
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response

@Suppress("NAME_SHADOWING")
class CollectRegiActivity : AppCompatActivity() {
    private lateinit var mBinding: ActCollectRegiBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener
    private lateinit var mLoginInfo: LoginResponseModel

    private var cash = true
    private var note = false
    private var both = false
    private var customerCd: String? = ""
    private var customerNm: String? = ""
    private var balanceData: BalanceModel? = null
    private var collectionCd: String? = null
    private var billType: String? = null

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
       override fun handleMessage(msg: Message) {
           super.handleMessage(msg)
           val value = msg.obj as String
           handleValueFromDialog(value)
       }
    }
    private var isSave = true // 액티비티가 종료 될 때 이 값을 통해 저장 여부 선택

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectRegiBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()

        // UI 셋팅
        setUI()

        customerNm = intent.getStringExtra("customerNm") ?: ""
        customerCd = intent.getStringExtra("customerCd") ?: ""
        if (customerCd != "" && customerNm != "") {
            getCustomerBond(customerCd!!)
            mBinding.tvAccountName.text = customerNm
            mBinding.etAccount.visibility = View.GONE
            mBinding.tvAccountName.visibility = View.VISIBLE
            mBinding.btEmpty.visibility = View.VISIBLE
            mBinding.tvAccountName.isSelected = true
        }

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if (balanceData != null) {
                    PopupNoticeV2(mContext, "기존 주문이 완료되지 않았습니다.\n전표를 저장하시겠습니까?",
                        object : Handler(Looper.getMainLooper()) {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun handleMessage(msg: Message) {
                                when (msg.what) {
                                    Define.EVENT_OK -> {
                                        saveData()
                                        finish()
                                    }
                                    Define.EVENT_CANCEL -> {
                                        isSave = false
                                        SharedData.setSharedData(mContext, "collectCustomerCd", "")
                                        SharedData.setSharedData(mContext, "collectCustomerNm", "")
                                        finish()
                                    }
                                }
                            }
                        }
                    ).show()
                } else {
                    finish()
                }
            }
        })

        mBinding.radioGroup.setOnCheckedChangeListener(radioGroupCheckedListener)

        mBinding.typeText.setOnClickListener {
            val dlg = PopupNoteType(this,  handler)
            dlg.show()
        }

        mBinding.bottom.bottomButton.setOnClickListener {
            if (emptyCheck()) {
                val dlg = PopupOrderSend(this, object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        when(msg.what) {
                            Define.EVENT_OK -> {
                                postSlip()
                            }
                        }
                    }
                })
                dlg.show()
            }
        }

        mBinding.btEmpty.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.etAccount.text = null
                mBinding.etAccount.hint = mContext.getString(R.string.accountHint)
                mBinding.btEmpty.visibility = View.GONE
                mBinding.tvAccountName.visibility = View.GONE
                mBinding.etAccount.visibility = View.VISIBLE
                mBinding.uncollected.text = null
                mBinding.uncollected.hint = mContext.getString(R.string.accountHint)
                mBinding.uncollected.gravity = Gravity.CENTER_VERTICAL
                mBinding.collectedDate.text = mContext.getString(R.string.accountHint)
                mBinding.totalAmount.text = mContext.getString(R.string.accountHint)
                mBinding.totalAmount.gravity = Gravity.CENTER_VERTICAL
                customerCd = ""
                customerNm = ""
                balanceData = null
            }
        })

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            @SuppressLint("SetTextI18n")
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountListSearch(mContext, mBinding.etAccount.text.toString())
                popupAccountSearch.onItemSelect = {
                    mBinding.etAccount.visibility = View.GONE
                    mBinding.tvAccountName.visibility = View.VISIBLE
                    mBinding.btEmpty.visibility = View.VISIBLE
                    mBinding.tvAccountName.isSelected = true
                    mBinding.tvAccountName.text = "(${it.custCd}) ${it.custNm}"
                    customerCd = it.custCd
                    customerNm = "(${it.custCd}) ${it.custNm}"
                    getCustomerBond(it.custCd)
                }
                popupAccountSearch.show()
            }
        })

        mBinding.etAccount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mBinding.search.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun setUI() {
        //헤더 및 바텀 설정
        mBinding.header.headerTitle.text = getString(R.string.collectRegi)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)

        mBinding.bottom.bottomButton.text = getString(R.string.collectRegi)

        mBinding.etAccount.isSelected = true

        radioGroupCheckedListener = OnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.cash -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.GONE

                    cash = true
                    note = false
                    both = false
                    collectionCd = Define.CASH
                }
                R.id.note -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.GONE
                    mBinding.remark.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.VISIBLE

                    cash = false
                    note = true
                    both = false
                    collectionCd = Define.NOTE
                }
                R.id.both -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.VISIBLE

                    cash = false
                    note = false
                    both = true
                    collectionCd = Define.NOTE
                }
            }
        }
    }

    private fun handleValueFromDialog(value: String) {
        mBinding.typeText.text = value
        when (value) {
            mContext.getString(R.string.promissory) -> {
                billType = "0001"
            }
            mContext.getString(R.string.listed) -> {
                billType = "0002"
            }
            mContext.getString(R.string.householdCheck) -> {
                billType = "0003"
            }
            mContext.getString(R.string.currentCheck) -> {
                billType = "0004"
            }
        }
    }

    private fun emptyCheck(): Boolean {
        if (mBinding.etAccount.text.isEmpty()
            || mBinding.uncollected.text.isEmpty()
            || mBinding.collectedDate.text.isEmpty()
            || mBinding.totalAmount.text.isEmpty()){
            Utils.popupNotice(mContext, "필수 입력란이 비었습니다.")
        } else {
            if (cash && mBinding.cashAmountText.text!!.isEmpty()) {
                Utils.popupNotice(mContext, "필수 입력란이 비었습니다.")
                return false
            } else if(note && (mBinding.typeText.text.isEmpty()
                        || mBinding.noteAmountText.text?.isEmpty()!!
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text?.isEmpty()!!
                        || mBinding.expireDateText.text.isEmpty())) {
                Utils.popupNotice(mContext, "필수 입력란이 비었습니다.")
                return false
            } else if (both && (mBinding.cashAmountText.text!!.isEmpty()
                        || mBinding.typeText.text.isEmpty()
                        || mBinding.noteAmountText.text?.isEmpty()!!
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text?.isEmpty()!!
                        || mBinding.expireDateText.text.isEmpty())){
                Utils.popupNotice(mContext, "필수 입력란이 비었습니다.")
                return false
            } else if (!cash && !note && !both) {
                Utils.popupNotice(mContext, "수금 수단을 선택해주세요.")
            }
        }
        return true
    }

    private fun getCustomerBond(customerCd: String){
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.customerBond(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, customerCd)
        //test
        //val call = service.customerBond("C000028", "mb2004", "000989")

        call.enqueue(object : retrofit2.Callback<ResultModel<BalanceModel>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ResultModel<BalanceModel>>,
                response: Response<ResultModel<BalanceModel>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            balanceData = item.data.copy(
                                bondBalance = item.data.bondBalance,
                                lastCollectionDate = item.data.lastCollectionDate,
                                lastCollectionAmount = item.data.lastCollectionAmount
                            )
                            Utils.log("bond balance search success ====> $balanceData")
                            mBinding.uncollected.text = "${Utils.decimal(balanceData?.bondBalance!!)}원"
                            mBinding.uncollected.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                            mBinding.collectedDate.text = balanceData?.lastCollectionDate
                            mBinding.totalAmount.text = "${Utils.decimal(balanceData?.lastCollectionAmount!!)}원"
                            mBinding.totalAmount.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg)
                            Utils.log("returnCd ====> ${item.returnCd}")
                        }
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }
            override fun onFailure(call: Call<ResultModel<BalanceModel>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("getInfo failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }
        })
    }

    private fun postSlip() {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)

        val agencyCd = mLoginInfo.agencyCd
        val userId = mLoginInfo.userId
        val customerCd = customerCd
        val collectionDate = Utils.getCurrentDateFormatted()
        val remark = if(mBinding.remarkText.text.isNotEmpty()) mBinding.remarkText.text.toString() else { null }

        val json = if (cash) {
            val cashAmount = mBinding.cashAmountText.text.toString().replace(",", "").toInt()
            val remark = mBinding.remarkText.text.toString()
            JsonObject().apply {
                addProperty("agencyCd", agencyCd)
                addProperty("userId", userId)
                addProperty("customerCd", customerCd)
                addProperty("collectionDate", collectionDate)
                addProperty("collectionAmount", cashAmount)
                addProperty("collectionCd", collectionCd)
                addProperty("cashAmount", cashAmount)
                addProperty("remark", remark)
            }
        } else if (note) {
            val billAmount = mBinding.noteAmountText.text.toString().replace(",", "").toInt()
            val billNo = mBinding.noteNumberText.text.toString()
            val billIssuer = mBinding.publishByText.text.toString()
            val billIssueDate = mBinding.publishDateText.text.toString()
            val billExpireDate = mBinding.expireDateText.text.toString()
            JsonObject().apply {
                addProperty("agencyCd", agencyCd)
                addProperty("userId", userId)
                addProperty("customerCd", customerCd)
                addProperty("collectionDate", collectionDate)
                addProperty("collectionAmount", billAmount)
                addProperty("collectionCd", collectionCd)
                addProperty("billAmount", billAmount)
                addProperty("billType", billType)
                addProperty("billNo", billNo)
                addProperty("billIssuer", billIssuer)
                addProperty("billIssueDate", billIssueDate)
                addProperty("billExpireDate", billExpireDate)
                addProperty("remark", remark)
            }
        } else {
            val cashAmount = mBinding.cashAmountText.text.toString().replace(",", "").toInt()
            val billAmount = mBinding.noteAmountText.text.toString().replace(",", "").toInt()
            val collectionAmount = cashAmount + billAmount
            val billNo = mBinding.noteNumberText.text.toString()
            val billIssuer = mBinding.publishByText.text.toString()
            val billIssueDate = mBinding.publishDateText.text.toString()
            val billExpireDate = mBinding.expireDateText.text.toString()
            val remark = mBinding.remarkText.text.toString()
            JsonObject().apply {
                addProperty("agencyCd", agencyCd)
                addProperty("userId", userId)
                addProperty("customerCd", customerCd)
                addProperty("collectionDate", collectionDate)
                addProperty("collectionAmount", collectionAmount)
                addProperty("collectionCd", collectionCd)
                addProperty("cashAmount", cashAmount)
                addProperty("billType", billType)
                addProperty("billNo", billNo)
                addProperty("billIssuer", billIssuer)
                addProperty("billIssueDate", billIssueDate)
                addProperty("billExpireDate", billExpireDate)
                addProperty("remark", remark)
            }
        }

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.slipAdd(body)
        Utils.log("slip request body ====> ${Gson().toJson(json)}")

        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ResultModel<DataModel<Unit>>>,
                response: Response<ResultModel<DataModel<Unit>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.log("item ====> ${item.data}")
                        Utils.log("slip add success ====> ${Gson().toJson(item)}")
                        Utils.toast(mContext, "수금 등록이 전송되었습니다.")
                        // 주문이 전송되면 데이터 초기화
                        isSave = false
                        SharedData.setSharedData(mContext, "collectCustomerCd", "")
                        SharedData.setSharedData(mContext, "collectCustomerNm", "")

                        val moneySlipNo = item.data.moneySlipNo
                        val intent = Intent(mContext, PrinterOptionActivity::class.java).apply {
                            putExtra("moneySlipNo", moneySlipNo)
                            putExtra("title", mContext.getString(R.string.titleCollect))
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Utils.log("return message ====> ${item?.returnMsg}")
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DataModel<Unit>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("slip add failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }
        })
    }

    private fun saveData() {
        SharedData.setSharedData(mContext, "collectCustomerCd", customerCd ?: "")
        SharedData.setSharedData(mContext, "collectCustomerNm", customerNm ?: "")
    }

    override fun onStop() {
        super.onStop()
        if (customerCd != "" && isSave) {
            saveData()
        }
    }
}