package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.widget.Button
import java.util.Calendar

class PopupDatePicker(private val context: Context) {

    @SuppressLint("SetTextI18n")
    fun showDatePickerDialog(text: Button) {
        val today = Calendar.getInstance()
        val year: Int = today.get(Calendar.YEAR)
        val month: Int = today.get(Calendar.MONTH)
        val date: Int = today.get(Calendar.DATE)

        val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            text.text = "${year}-${month + 1}-${dayOfMonth}"
        }, year, month, date)

        datePickerDialog.show()
    }
}