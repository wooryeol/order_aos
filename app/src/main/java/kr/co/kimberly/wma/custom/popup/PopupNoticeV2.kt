package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import kr.co.kimberly.wma.common.Bluetooth
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupNoticeBinding
import kr.co.kimberly.wma.databinding.PopupNoticeV2Binding
import kr.co.kimberly.wma.menu.setting.SettingActivity

class PopupNoticeV2(mContext: Context, private val msg:String, val mHandler: Handler): Dialog(mContext) {
    private lateinit var mBinding: PopupNoticeV2Binding
    private val context = mContext
    var itemClickListener: ItemClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupNoticeV2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private fun initViews() {
        setCancelable(false) // 뒤로가기 버튼, 바깥 화면 터치시 닫히지 않게

        // (중요) Dialog 는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius 가 보이지 않음
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        mBinding.tvMsg.text = msg

        mBinding.ok.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mHandler.sendEmptyMessage(Define.OK)
                hideDialog()
            }
        })

        mBinding.cancel.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                hideDialog()
            }
        })


    }

    fun hideDialog() {
        if (isShowing) {
            dismiss()
        }
    }

    interface ItemClickListener {
        fun onOkClick()
    }
}