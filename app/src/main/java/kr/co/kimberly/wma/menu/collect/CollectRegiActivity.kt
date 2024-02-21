package kr.co.kimberly.wma.menu.collect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupDatePicker
import kr.co.kimberly.wma.custom.popup.PopupNoteType
import kr.co.kimberly.wma.custom.popup.PopupOrderSend
import kr.co.kimberly.wma.databinding.ActCollectRegiBinding

class CollectRegiActivity : AppCompatActivity() {

    private lateinit var mBinding: ActCollectRegiBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private var cash = false
    private var note = false
    private var both = false

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

        //헤더 및 바텀 설정
        mBinding.header.headerTitle.text = getString(R.string.collectRegi)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })
        mBinding.bottom.bottomButton.text = getString(R.string.collectRegi)

        mBinding.typeText.setOnClickListener {
            val dlg = PopupNoteType(this, mActivity, handler)
            dlg.show()
        }

        mBinding.bottom.bottomButton.setOnClickListener {
            if (emptyCheck()) {
                val dlg = PopupOrderSend(this, mActivity)
                dlg.show()
            }
        }

        // 날짜 선택
        val datePickerDialog = PopupDatePicker(this)
        mBinding.collectedDate.setOnClickListener {
            datePickerDialog.initCustomDatePicker(mBinding.collectedDate)
        }

        //현금 선택 화면이 먼저 보이도록
        mBinding.cash.isChecked = true

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
    }

    private fun handleValueFromDialog(value: String) {
        mBinding.typeText.text = value
    }

    // 각 라디오 버튼을 눌렀을 때 보여주는 xml을 다르게
    fun onCollectActRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when(view.id) {
                R.id.cash -> {
                    if (checked) {
                        mBinding.cashBox.visibility = View.VISIBLE
                        mBinding.cashAmount.visibility = View.VISIBLE
                        mBinding.noteBox.visibility = View.GONE

                        cash = true
                        note = false
                        both = false
                    }
                }
                R.id.note -> {
                    if (checked) {
                        mBinding.cashBox.visibility = View.VISIBLE
                        mBinding.cashAmount.visibility = View.GONE
                        mBinding.remark.visibility = View.VISIBLE
                        mBinding.noteBox.visibility = View.VISIBLE

                        cash = false
                        note = true
                        both = false
                    }
                }
                R.id.both -> {
                    if (checked) {
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
    }

    private fun emptyCheck(): Boolean {
        if (mBinding.accountName.text.isEmpty()
            || mBinding.uncollected.text.isEmpty()
            || mBinding.collectedDate.text.isEmpty()
            || mBinding.totalAmount.text.isEmpty()){
            Toast.makeText(mContext, "필수 입력란이 비었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            if (cash && mBinding.cashAmountText.text.isEmpty()) {
                Toast.makeText(mContext, "필수 입력란이 비었습니다.", Toast.LENGTH_SHORT).show()
                return false
            } else if(note && (mBinding.typeText.text.isEmpty()
                        || mBinding.noteAmountText.text.isEmpty()
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text.isEmpty()
                        || mBinding.expireDateText.text.isEmpty())) {
                Toast.makeText(mContext, "필수 입력란이 비었습니다.", Toast.LENGTH_SHORT).show()
                return false
            } else if (both && (mBinding.cashAmountText.text.isEmpty()
                        || mBinding.typeText.text.isEmpty()
                        || mBinding.noteAmountText.text.isEmpty()
                        || mBinding.noteNumberText.text.isEmpty()
                        || mBinding.publishByText.text.isEmpty()
                        || mBinding.publishDateText.text.isEmpty()
                        || mBinding.expireDateText.text.isEmpty())){
                Toast.makeText(mContext, "필수 입력란이 비었습니다.", Toast.LENGTH_SHORT).show()
                return false
            } else if (!cash && !note && !both) {
                Toast.makeText(mContext, "수금 수단을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}