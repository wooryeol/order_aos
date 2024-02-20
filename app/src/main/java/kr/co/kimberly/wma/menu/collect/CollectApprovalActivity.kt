package kr.co.kimberly.wma.menu.collect

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupPrintDone
import kr.co.kimberly.wma.databinding.ActCollectApprovalBinding
import kr.co.kimberly.wma.menu.main.MainActivity

class CollectApprovalActivity : AppCompatActivity() {

    private lateinit var mBinding: ActCollectApprovalBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActCollectApprovalBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        // 헤더 및 바텀 설정
        mBinding.header.headerTitle.text = getString(R.string.orderApproval)
        mBinding.header.scanBtn.visibility = View.GONE
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

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