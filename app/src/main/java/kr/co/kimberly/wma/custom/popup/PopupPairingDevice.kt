package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import kr.co.kimberly.wma.databinding.PopupPairingScannerBinding
import kr.co.kimberly.wma.databinding.PopupParingPrinterBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity

class PopupPairingDevice(private val mContext: Context, mActivity: Activity) {

    private var scannerBinding: PopupPairingScannerBinding? = null
    private var printerBinding: PopupParingPrinterBinding? = null
    private val mDialog = Dialog(mContext)

    @SuppressLint("MissingPermission")
    fun show(item: BluetoothDevice) {
        when(SettingActivity.isRadioChecked) {
            1 -> {
                scannerBinding = PopupPairingScannerBinding.inflate(LayoutInflater.from(mContext))

                mDialog.setCancelable(false)
                mDialog.setContentView(scannerBinding!!.root)

                mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                scannerBinding?.deviceAddress?.text = item.address
                scannerBinding?.deviceName?.text = item.name

                scannerBinding?.isPairing?.playAnimation()

                mDialog.show()
            }

            /*2 -> {
                printerBinding = PopupParingPrinterBinding.inflate(LayoutInflater.from(mContext))

                mDialog.setCancelable(false)
                mDialog.setContentView(printerBinding!!.root)
                // mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

                mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

                item.createBond()

                printerBinding!!.deviceName.text = item.name

                printerBinding!!.cancelBtn.setOnClickListener {
                    mDialog.dismiss()
                }

                printerBinding!!.confirmBtn.setOnClickListener {
                    Log.d("wooryeol", "확인 버튼이 클릭되었습니다.")
                }

                printerBinding!!.checkBoxPin.setOnClickListener{
                    Log.d("wooryeol", "체크 버튼이 클릭되었습니다.")
                }

                mDialog.show()
            }*/
        }

        item.createBond()

        if (item.bondState == 12) {
            hideDialog()
        }
    }

    fun hideDialog() {
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
    }
}