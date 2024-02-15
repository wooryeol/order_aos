package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.LinearLayout
import kr.co.kimberly.wma.databinding.PopupPairingScannerBinding
import kr.co.kimberly.wma.databinding.PopupParingPrinterBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity

class PopupPairingDevice(context: Context, activity: Activity) {

    private var scannerBinding: PopupPairingScannerBinding? = null
    private var printerBinding: PopupParingPrinterBinding? = null
    private var mContext = context
    var mActivity = activity

    fun show(deviceName: String, deviceAddress: String) {
        val mDialog = Dialog(mContext)
        when(SettingActivity.isRadioChecked) {
            1 -> {
                scannerBinding = PopupPairingScannerBinding.inflate(LayoutInflater.from(mContext))

                mDialog.setCancelable(false)
                mDialog.setContentView(scannerBinding!!.root)

                mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


                /*val height = Resources.getSystem().displayMetrics.heightPixels * 0.3
                mDialog.window?.setLayout(960, height.toInt())*/

                scannerBinding!!.deviceAddress.text = deviceAddress
                scannerBinding!!.deviceName.text = deviceName

                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    mDialog.dismiss()
                }, 3000)

                mDialog.show()
            }

            2 -> {
                printerBinding = PopupParingPrinterBinding.inflate(LayoutInflater.from(mContext))

                mDialog.setCancelable(false)
                mDialog.setContentView(printerBinding!!.root)
                // mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

                mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


                /*val height = Resources.getSystem().displayMetrics.heightPixels * 0.45
                mDialog.window?.setLayout(960, height.toInt())*/

                /*printerBinding!!.deviceName.text = deviceName

                printerBinding!!.cancelBtn.setOnClickListener {
                    mDialog.dismiss()
                }

                printerBinding!!.confirmBtn.setOnClickListener {
                    Log.d("wooryeol", "확인 버튼이 클릭되었습니다.")
                }

                printerBinding!!.checkBoxPin.setOnClickListener{
                    Log.d("wooryeol", "체크 버튼이 클릭되었습니다.")
                }*/

                mDialog.show()
            }
        }
    }
}