package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupNoticeBinding
import kr.co.kimberly.wma.databinding.PopupOrderSendBinding
import kr.co.kimberly.wma.menu.collect.CollectApprovalActivity

class PopupError(private val mContext: AppCompatActivity, private val mActivity: Activity) {

    private lateinit var mBinding: PopupNoticeBinding
    private val mDialog = Dialog(mContext)


    fun show() {
        mBinding = PopupNoticeBinding.inflate(mContext.layoutInflater)

        mDialog.setCancelable(false)
        mDialog.setContentView(mBinding.root)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        mBinding.tvMsg.text = mContext.getString(R.string.error)

        mBinding.btConfirm.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.show()
    }
}