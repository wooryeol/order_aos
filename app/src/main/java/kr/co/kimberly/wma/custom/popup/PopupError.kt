package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupErrorBinding
import kr.co.kimberly.wma.databinding.PopupOrderSendBinding
import kr.co.kimberly.wma.menu.collect.CollectApprovalActivity

class PopupError(private val mContext: AppCompatActivity, private val mActivity: Activity) {

    private lateinit var mBinding: PopupErrorBinding
    private val mDialog = Dialog(mContext)


    fun show() {
        mBinding = PopupErrorBinding.inflate(mContext.layoutInflater)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setContentView(mBinding.root)
        mDialog.setCancelable(false)
        mDialog.show()

        val height = Resources.getSystem().displayMetrics.heightPixels * 0.28
        mDialog.window?.setLayout(960, height.toInt())

        mBinding.cancel.setOnClickListener {
            mDialog.dismiss()
        }
    }
}