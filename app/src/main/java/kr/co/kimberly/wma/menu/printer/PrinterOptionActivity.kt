package kr.co.kimberly.wma.menu.printer

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.custom.popup.PopupPrintDone
import kr.co.kimberly.wma.databinding.ActPrinterOptionBinding

class PrinterOptionActivity : AppCompatActivity() {
    private lateinit var mBinding: ActPrinterOptionBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPrinterOptionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.orderApproval)
        mBinding.header.scanBtn.visibility = View.GONE

        // 인쇄
        mBinding.printBtn.setOnClickListener {
            if (mBinding.printQuantity.text.isNotEmpty()) {
                val dlg = PopupPrintDone(this, mActivity)
                dlg.show()
            } else {
                Toast.makeText(mContext, "인쇄 수량을 적어주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}