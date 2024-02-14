package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
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
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setContentView(mBinding.root)
        mDialog.setCancelable(false)
        mDialog.show()

        val height = Resources.getSystem().displayMetrics.heightPixels * 0.25
        mDialog.window?.setLayout(960, height.toInt())

        mBinding.cancel.setOnClickListener {
            mDialog.dismiss()
        }

        mBinding.order.setOnClickListener {
            val intent =  Intent(mContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mContext.startActivity(intent)
            Toast.makeText(mContext, "인쇄를 진행합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}