package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.databinding.PopupDatePickerBinding
import java.util.Calendar

class PopupDatePicker(private val mContext: AppCompatActivity) {

    private lateinit var mBinding : PopupDatePickerBinding
    private val mDialog = Dialog(mContext)

    private val today: Calendar = Calendar.getInstance()
    private var year: Int = today.get(Calendar.YEAR)
    private var month: Int = today.get(Calendar.MONTH)
    private var date: Int = today.get(Calendar.DATE)

    // 날짜 갱신 함수 정의
    private fun updateDate() {
        today.set(year, month - 1, 1) // 선택한 월의 1일로 설정하여 해당 월의 마지막 날짜를 가져옴
        val maxDate = today.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (date > maxDate) {
            date = maxDate // 선택한 월의 마지막 날짜를 넘어가면 마지막 날짜로 설정
        }
        mBinding.year.text = year.toString()
        mBinding.month.text = month.toString().padStart(2, '0')
        mBinding.date.text = date.toString().padStart(2, '0')
    }

    // 직전 한달만 가져오기
    private fun getPreviousMonthDate() {
        // 현재 날짜에서 한 달을 뺌
        val previousMonth = Calendar.getInstance()
        previousMonth.set(year, month - 2, date) // month는 0부터 시작하므로 -1 해줌

        // 갱신된 날짜 설정
        year = previousMonth.get(Calendar.YEAR)
        month = previousMonth.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작하므로 +1 해줌
        date = previousMonth.get(Calendar.DATE)

        updateDate()
    }

    @SuppressLint("SetTextI18n")
    fun initCustomDatePicker(text: Button, noDate: Boolean? = null) {
        mBinding = PopupDatePickerBinding.inflate(mContext.layoutInflater)

        mDialog.setCancelable(false)
        mDialog.setContentView(mBinding.root)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        if (noDate != null) {
            if (noDate) {
                mBinding.layoutDate.visibility = View.GONE
            }
        }

        // 초기화된 날짜 설정
        updateDate()

        // 년도 조절 버튼
        mBinding.btnAddYear.setOnClickListener {
            year += 1
            updateDate()
        }

        mBinding.btnMinusYear.setOnClickListener {
            year -= 1
            updateDate()
        }

        // 월 조절 버튼
        mBinding.btnAddMonth.setOnClickListener {
            month += 1
            if (month > 12) {
                month = 1 // 12월을 넘어가면 1월로 설정
                year += 1 // 연도도 증가
            }
            updateDate()
        }

        mBinding.btnMinusMonth.setOnClickListener {
            month -= 1
            if (month < 1) {
                month = 12 // 1월 미만이면 12월로 설정
                year -= 1 // 연도도 감소
            }
            updateDate()
        }

        // 일 조절 버튼
        mBinding.btnAddDate.setOnClickListener {
            date += 1
            updateDate()
        }

        mBinding.btnMinusDate.setOnClickListener {
            date -= 1
            if (date < 1) {
                date = 1 // 1일 미만이면 1일로 설정
            }
            updateDate()
        }

        //  취소 버튼 클릭 시
        mBinding.cancelBtn.setOnClickListener {
            mDialog.dismiss()
        }

        //  완료 버튼 클릭 시
        mBinding.confirmBtn.setOnClickListener {
            val selectedMonth = month.toString().padStart(2, '0')
            val selectedDate = date.toString().padStart(2, '0')
            if (noDate != null) {
                if (noDate) {
                    text.text = "$year-$selectedMonth"
                }
            } else {
                text.text = "$year/$selectedMonth/$selectedDate"
            }
            mDialog.dismiss()
        }

        mDialog.show()
    }
}