package kr.co.kimberly.wma.custom

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.text.DecimalFormat

class IntComma(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {

    private var inputText = ""

    init {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (!TextUtils.isEmpty(text) && !TextUtils.equals(
                        inputText,
                        text
                    )
                ) {

                    var strNumber: String   // 정수부
                    var strDecimal = ""     // 소수부

                    if (text.contains(".")) {
                        strNumber = text.substring(0, text.indexOf("."))
                        strDecimal = text.substring(text.indexOf("."), text.length)
                    } else {
                        strNumber = text
                    }

                    strNumber = strNumber.replace(",", "")
                    val doubleText = strNumber.toDoubleOrNull() ?: 0.0
                    val decimalFormat = DecimalFormat("#,###")

                    inputText = decimalFormat.format(doubleText) + strDecimal
                    setText(inputText)
                    setSelection(inputText.length)
                }
            }
        })
    }
}