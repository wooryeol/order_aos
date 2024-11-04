package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.widget.LinearLayout
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupPairingScannerBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity

class PopupPairingDevice(private val mContext: Context, private val listener: SettingActivity.PopupListener) {
    private var scannerBinding: PopupPairingScannerBinding? = null
    private val mDialog = Dialog(mContext)
    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val paired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                if (paired?.bondState == BluetoothDevice.BOND_BONDED) {
                    Utils.toast(mContext, "기기가 연결되었습니다.")
                    listener.popupClosed()
                    mDialog.dismiss()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun show(item: BluetoothDevice) {
        scannerBinding = PopupPairingScannerBinding.inflate(LayoutInflater.from(mContext))

        mDialog.setCancelable(true)
        mDialog.setContentView(scannerBinding!!.root)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        scannerBinding?.deviceAddress?.text = item.address
        scannerBinding?.deviceName?.text = item.name

        scannerBinding?.isPairing?.playAnimation()
        item.createBond()
        val searchFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        mContext.registerReceiver(bluetoothReceiver, searchFilter)

        mDialog.show()
    }
}