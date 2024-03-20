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
import android.media.Image
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupSearchDevices
import kr.co.kimberly.wma.custom.popup.PopupSingleMessage
import kr.co.kimberly.wma.menu.setting.SettingActivity

class BluetoothV2(context: Context, activity: Activity, private val list: ArrayList<BluetoothDevice>? = null, private val adapter: RecyclerView.Adapter<*>? = null, private val isPaired: Boolean, private val loading: ImageView? = null) {
    private val mContext = context
    private val mActivity = activity

    private val bluetoothManager:BluetoothManager by lazy {
        mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    val mBluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private var permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
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
            if (!isPaired) {
                when(intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Log.d("test log", "bluetooth 가능 기기를 탐색합니다.")
                        Toast.makeText(mContext, "bluetooth 가능 기기를 탐색합니다.", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        device?.let {
                            if (it.name != null) {
                                Log.d("test log", "found name >>> ${device.name}")
                                Log.d("test log", "found address >>> ${device.address}")

                                if (it.bondState == 10 && list?.contains(it) == false) {
                                    if (it.name.startsWith("Alpha") && SettingActivity.isRadioChecked == 2) {
                                        list.add(it)
                                    }
                                    /*else if (it.name.startsWith("KDC") && SettingActivity.isRadioChecked == 1) {
                                        list.add(it)
                                    }*/
                                    else if (!it.name.startsWith("Alpha") && SettingActivity.isRadioChecked == 1) {
                                        list.add(it)
                                    }
                                }
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.d("test log", "bluetooth 가능 기기 탐색을 종료합니다.")
                        Toast.makeText(mContext, "bluetooth 가능 기기 탐색을 종료합니다.", Toast.LENGTH_SHORT).show()
                        loading?.visibility = View.GONE
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
                            SharedData.setSharedData(mContext, SharedData.PRINTER_NAME, paired.name)
                            SharedData.setSharedData(mContext, SharedData.PRINTER_ADDR, paired.address)
                        }
                    }
                }
            }
        }
    }


    private fun bluetoothPermission(){
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    discovery()
                }
                @SuppressLint("MissingPermission")
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    val popupNotice = PopupNotice(mContext, "블루투스 권한을 확인해주세요.", true)
                    popupNotice.show()
                }
            })
            .setPermissions(*permissions)
            .check()
    }


    @SuppressLint("MissingPermission")
    fun checkBluetoothAvailable() {
        if (mBluetoothAdapter == null) {
            val popupNotice = PopupNotice(mContext, "블루투스를 지원하지 않는 기기입니다.")
            popupNotice.show()
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                mActivity.startActivityForResult(enableBtIntent, Define.REQUEST_ENABLE_BT)
                return
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
        loading?.visibility = View.VISIBLE
        mContext.registerReceiver(mBluetoothReceiver, searchFilter)
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun discovery() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            bluetoothPermission()
        } else if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        } else {
            list?.clear()
            if (isPaired) {
                val pairedDevices: Set<BluetoothDevice>? = mBluetoothAdapter.bondedDevices
                pairedDevices?.let {
                    it.forEach { device ->
                        Log.d("test log", "pairedDevices name  >>> ${device.name}")
                        Log.d("test log", "pairedDevices address >>> ${device.address}")
                        list?.add(device)
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
            loading?.visibility = View.VISIBLE
            mBluetoothAdapter.startDiscovery()
        }
    }

    private fun checkBluetooth(): ActivityResultLauncher<Intent> =
        AppCompatActivity().registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                discovery()
            } else if (it.resultCode == RESULT_CANCELED) {
                val popupNotice = PopupNotice(mContext, "블루투스 기능이 켜져있는지 확인 해주세요")
                popupNotice.show()
            }
        }
}