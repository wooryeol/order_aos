package kr.co.kimberly.wma.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.menu.setting.SettingActivity

class BluetoothCheck(context: AppCompatActivity, private val activity: Activity) {
    private val mContext = context
    private val mActivity = activity
    private val bluetoothManager: BluetoothManager by lazy {
        mContext.getSystemService(BluetoothManager::class.java)
    }
    val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private var permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun initBluetooth() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    checkBluetooth()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            })
            .setDeniedMessage("블루투스 권한을 허용해주세요.\n[설정] > [애플리케이션] > [앱 권한]")
            .setPermissions(*permissions)
            .check()
    }
    @SuppressLint("HardwareIds")
    fun checkBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                mContext.startActivityForResult(enableBtIntent, Define.REQUEST_ENABLE_BT)
            }
        } else {
            discovery()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun discovery(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            SettingActivity.searchedList.clear()
            val pairedDevice = bluetoothAdapter.bondedDevices
            pairedDevice?.let {
                it.forEach {device ->
                    val name = device.name
                    val address = device.address
                    PopupSearchDevices(mContext, mActivity).adapter.notifyDataSetChanged()
                }
            }
            bluetoothAdapter.startDiscovery()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission", "NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.name != null) {
                        if (!SettingActivity.searchedList.contains(device)) SettingActivity.searchedList.add(device)

                        /*Log.d("wooryeol", "deviceName >>> $deviceName")
                        Log.d("wooryeol", "deviceHardwareAddress >>> $deviceHardwareAddress")*/

                        PopupSearchDevices(mContext, mActivity).adapter.notifyDataSetChanged()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if(bluetoothAdapter.isDiscovering) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val paired : BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (paired?.bondState == BluetoothDevice.BOND_BONDED) {
                        PopupSearchDevices(mContext, mActivity).adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}