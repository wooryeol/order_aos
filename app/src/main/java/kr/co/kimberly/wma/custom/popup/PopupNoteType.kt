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
import kr.co.kimberly.wma.databinding.PopupNoteTypeBinding

class PopupNoteType(private val mContext: AppCompatActivity, private val mActivity: Activity, handler: Handler) {

    private lateinit var mBinding: PopupNoteTypeBinding
    private val mDialog = Dialog(mContext)
    private val mHandler = handler

    fun show() {
        mBinding = PopupNoteTypeBinding.inflate(mContext.layoutInflater)

        mDialog.setCancelable(true)
        mDialog.setContentView(mBinding.root)
        // mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        setupTextViewClickListeners()

        mDialog.show()
    }

    private fun setupTextViewClickListeners() {
        val message = android.os.Message.obtain()
        mBinding.promissory.setOnClickListener {
            val type = mBinding.promissory.text.toString()
            message.obj = type
            mHandler.sendMessage(message)
            mDialog.dismiss()
        }

        mBinding.listed.setOnClickListener {
            val type = mBinding.listed.text.toString()
            message.obj = type
            mHandler.sendMessage(message)
            mDialog.dismiss()
        }

        mBinding.householdCheck.setOnClickListener {
            val type = mBinding.householdCheck.text.toString()
            message.obj = type
            mHandler.sendMessage(message)
            mDialog.dismiss()
        }

        mBinding.currentCheck.setOnClickListener {
            val type = mBinding.currentCheck.text.toString()
            message.obj = type
            mHandler.sendMessage(message)
            mDialog.dismiss()
        }
    }
}