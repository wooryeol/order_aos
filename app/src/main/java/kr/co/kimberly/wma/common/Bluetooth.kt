package kr.co.kimberly.wma.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.custom.popup.PopupSingleMessage

class Bluetooth(context: Context, activity: Activity, private val list: ArrayList<BluetoothDevice>? = null, private val adapter: RecyclerView.Adapter<*>? = null) {
    private val mContext = context
    private val mActivity = activity

    private val bluetoothManager:BluetoothManager by lazy {
        mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    val mBluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val searchFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        addAction(BluetoothDevice.ACTION_FOUND) //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
    }

    val mBluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission", "NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {

            when(intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Utils.log("bluetooth 가능 기기를 탐색합니다.")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let {
                        if (it.name != null) {
                            /*if (it.name.startsWith("Alpha")) {
                                list.add(it)
                            }*/

                            Utils.log("found name ====> ${device.name}")
                            Utils.log("found address ====> ${device.address}")
                            list?.add(it)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Utils.log("bluetooth 가능 기기 탐색을 종료합니다.")
                    if (mBluetoothAdapter.isDiscovering) {
                        mBluetoothAdapter.cancelDiscovery()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val paired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if (paired?.bondState == BluetoothDevice.BOND_BONDED) {
                        // loadingDialog.dismiss()

                        SharedData.setSharedData(mContext, SharedData.PRINTER_NAME, paired.name)
                        SharedData.setSharedData(mContext, SharedData.PRINTER_ADDR, paired.address)
                    }
                }
            }
        }
    }

    private var permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    } else  {
        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun initBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED ||
                mActivity.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
                mActivity.requestPermissions(permissions, 1000)
            } else {
                mBluetoothAdapter.let {
                    if (it.isEnabled) {
                        discovery()
                    } else {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        checkBluetooth().launch(intent)
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mActivity.requestPermissions(permissions, 1000)
            } else {
                mBluetoothAdapter.let {
                    if (it.isEnabled) {
                        discovery()
                    } else {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        checkBluetooth().launch(intent)
                    }
                }
            }
        } else {
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mActivity.requestPermissions(permissions, 1000)
            } else {
                mBluetoothAdapter.let {
                    if (it.isEnabled) {
                        discovery()
                    } else {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        checkBluetooth().launch(intent)
                    }
                }
            }
        }
        mContext.registerReceiver(mBluetoothReceiver, searchFilter)
    }

    private fun checkBluetooth(): ActivityResultLauncher<Intent> =
        AppCompatActivity().registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                discovery()
            } else if (it.resultCode == RESULT_CANCELED) {
                showNoticePopup("블루투스 기능이 켜져있는지 확인 해주세요")
            }

            // checkBluetoothPermission()
        }

    fun checkBluetoothPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    discovery()
                }

                @SuppressLint("MissingPermission")
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Utils.popupNotice(mContext, "블루투스 권한을 확인해주세요.")
                }
            })
            //.setDeniedMessage("블루투스 권한을 허용해주세요.\n[설정] > [애플리케이션] > [앱 권한]")
            .setPermissions(*permissions)
            .check()
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun discovery() {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        } else {

            val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter.bondedDevices
            pairedDevices?.let {
                it.forEach { device ->
                    Utils.log("pairedDevices name  ====> ${device.name}")
                    Utils.log("pairedDevices address >>> ${device.address}")
                }
            }
            mBluetoothAdapter.startDiscovery()
        }
    }

    private fun showNoticePopup(msg: String) {
        val popupNotice = PopupSingleMessage(mContext, msg = msg, mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Define.EVENT_OK -> {
                        mActivity.finish()
                    }
                }
            }
        })
        popupNotice.show()
    }
}