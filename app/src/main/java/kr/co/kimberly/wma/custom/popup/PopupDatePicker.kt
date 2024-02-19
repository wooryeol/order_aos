package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.databinding.PopupDatePickerBinding
import java.util.Calendar

class PopupDatePicker(private val context: Context) {
    @SuppressLint("SetTextI18n")
    fun showDatePickerDialog(text: Button) {
        val today = Calendar.getInstance()
        val year: Int = today.get(Calendar.YEAR)
        val month: Int = today.get(Calendar.MONTH)
        val date: Int = today.get(Calendar.DATE)

        val datePickerDialog = DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, { _, year, month, dayOfMonth ->
            text.text = "${year}-${month + 1}-${dayOfMonth}"
        }, year, month, date)

        datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun initCustomDatePicker(text: Button) {
        val dialog = AlertDialog.Builder(context).create()
        val mDialog : LayoutInflater = LayoutInflater.from(context)
        val mView : View = mDialog.inflate(R.layout.popup_date_picker,null)

        val year : NumberPicker = mView.findViewById(R.id.year)
        val month : NumberPicker = mView.findViewById(R.id.month)
        val day : NumberPicker = mView.findViewById(R.id.day)
        val save : Button = mView.findViewById(R.id.confirmBtn)
        val cancel : Button = mView.findViewById(R.id.cancelBtn)



        //  순환 안되게 막기
        year.wrapSelectorWheel = false
        month.wrapSelectorWheel = false
        day.wrapSelectorWheel = false

        //  editText 설정 해제
        year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        month.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        day.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        //  최소값 설정
        year.minValue = 1980
        month.minValue = 1
        day.minValue = 1

        //  최대값 설정
        year.maxValue = 2024
        month.maxValue = 12
        day.maxValue = 31


        //  취소 버튼 클릭 시
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }

        //  완료 버튼 클릭 시
        save.setOnClickListener {
            /*year_textview_statsfrag.text = (year.value).toString() + "년"
            month_textview_statsfrag.text = (month.value).toString() + "월"*/
            text.text = "$year/$month/$day"
            dialog.dismiss()
            dialog.cancel()
        }




        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
}