package kr.co.kimberly.wma.menu.collect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountListSearch
import kr.co.kimberly.wma.custom.popup.PopupNoteType
import kr.co.kimberly.wma.custom.popup.PopupOrderSend
import kr.co.kimberly.wma.databinding.ActCollectRegiBinding
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.BalanceModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ObjectResultModel
import retrofit2.Call
import retrofit2.Response

class CollectRegiActivity : AppCompatActivity() {
    private lateinit var mBinding: ActCollectRegiBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener
    private lateinit var mLoginInfo: LoginResponseModel

    private var cash = false
    private var note = false
    private var both = false
    private var balanceData: BalanceModel? = null

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
       override fun handleMessage(msg: Message) {
           super.handleMessage(msg)
           val value = msg.obj as String
           handleValueFromDialog(value)
       }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectRegiBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()!!

        // UI 셋팅
        setUI()

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val list: ArrayList<BalanceModel> = arrayListOf()
                if (balanceData != null) {
                    list.add(balanceData!!)
                    Utils.backBtnPopup(mContext, mActivity, list)
                } else {
                    Utils.backBtnPopup(mContext, mActivity, list)
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
                val dlg = PopupOrderSend(this, mActivity)
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
            }
        })

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            @SuppressLint("SetTextI18n")
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountListSearch(mContext, mBinding.etAccount.text.toString())
                popupAccountSearch.onItemSelect = {
                    mBinding.btEmpty.visibility = View.VISIBLE
                    mBinding.etAccount.visibility = View.GONE
                    mBinding.tvAccountName.visibility = View.VISIBLE
                    mBinding.tvAccountName.text = ("(${it.custCd}) ${it.custNm}")
                    mBinding.tvAccountName.isSelected = true
                    getCustomerBond(it.customerCd)
                }
                popupAccountSearch.show()
            }
        })
    }

    private fun setUI() {
        //헤더 및 바텀 설정
        mBinding.header.headerTitle.text = getString(R.string.collectRegi)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)

        mBinding.bottom.bottomButton.text = getString(R.string.collectRegi)

        mBinding.etAccount.isSelected = true

        radioGroupCheckedListener = OnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.cash -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.GONE

                    cash = true
                    note = false
                    both = false
                }
                R.id.note -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.GONE
                    mBinding.remark.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.VISIBLE

                    cash = false
                    note = true
                    both = false
                }
                R.id.both -> {
                    mBinding.cashBox.visibility = View.VISIBLE
                    mBinding.cashAmount.visibility = View.VISIBLE
                    mBinding.noteBox.visibility = View.VISIBLE

                    cash = false
                    note = false
                    both = true
                }
            }
        }
    }

    private fun handleValueFromDialog(value: String) {
        mBinding.typeText.text = value
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
                        || mBinding.noteAmountText.text.isEmpty()
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text.isEmpty()
                        || mBinding.expireDateText.text.isEmpty())) {
                Utils.popupNotice(mContext, "필수 입력란이 비었습니다.")
                return false
            } else if (both && (mBinding.cashAmountText.text!!.isEmpty()
                        || mBinding.typeText.text.isEmpty()
                        || mBinding.noteAmountText.text.isEmpty()
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text.isEmpty()
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
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        //val call = service.customerBond(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, customerCd)
        //test
        val call = service.customerBond("C000028", "mb2004", "000989")

        call.enqueue(object : retrofit2.Callback<ObjectResultModel<BalanceModel>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ObjectResultModel<BalanceModel>>,
                response: Response<ObjectResultModel<BalanceModel>>
            ) {
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {

                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            Utils.Log("bond balance search success ====> ${item.data}")
                            balanceData = item.data
                            mBinding.uncollected.text = "${Utils.decimal(balanceData?.bondBalance!!)}원"
                            mBinding.uncollected.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                            mBinding.collectedDate.text = balanceData?.lastCollectionDate
                            mBinding.totalAmount.text = "${Utils.decimal(balanceData?.lastCollectionAmount!!)}원"
                            mBinding.totalAmount.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg)
                        }
                    }
                } else {
                    Utils.Log("${response.code()} ====> ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ObjectResultModel<BalanceModel>>, t: Throwable) {
                Utils.Log("getInfo failed ====> ${t.message}")
            }

        })
    }
}