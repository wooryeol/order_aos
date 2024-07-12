package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupOrderSendBinding
import kr.co.kimberly.wma.databinding.PopupPrintDoneBinding
import kr.co.kimberly.wma.menu.collect.CollectApprovalActivity
import kr.co.kimberly.wma.menu.collect.CollectManageActivity
import kr.co.kimberly.wma.menu.main.MainActivity

class PopupPrintDone(private val mContext: AppCompatActivity, private val mActivity: Activity) {

    private lateinit var mBinding: PopupPrintDoneBinding
    private val mDialog = Dialog(mContext)


    fun show() {
        mBinding = PopupPrintDoneBinding.inflate(mContext.layoutInflater)

        mDialog.setCancelable(true)
        mDialog.setContentView(mBinding.root)
        // mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        mBinding.cancel.setOnClickListener {
            mDialog.dismiss()
        }

        mBinding.order.setOnClickListener {
            val intent =  Intent(mContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            mContext.startActivity(intent)
            Utils.toast(mContext, "인쇄를 진행합니다.")
        }

        mDialog.show()
    }
}