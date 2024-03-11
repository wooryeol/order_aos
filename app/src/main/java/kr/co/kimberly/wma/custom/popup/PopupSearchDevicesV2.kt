package kr.co.kimberly.wma.custom.popup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.SearchDevicesAdapterV2
import kr.co.kimberly.wma.databinding.PopupSearchDevicesV2Binding

class PopupSearchDevicesV2(mContext: Context, private val mActivity: AppCompatActivity, private val bluetoothOnResultLauncher: ActivityResultLauncher<Intent>): Dialog(mContext) {
    private lateinit var mBinding: PopupSearchDevicesV2Binding
    private lateinit var mAdapter: SearchDevicesAdapterV2
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothReceiver: BroadcastReceiver

    private val context = mContext
    // private var mBluetoothReceiver: BroadcastReceiver? = null

    override fun onStart() {
        super.onStart()

        val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        mBluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("pairedDevices", "intent.action ===> ${intent.action}")
                when (intent.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        // mBinding.activityHeader.btSearch.visibility = View.GONE
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        //검색한 블루투스 디바이스의 객체를 구한다
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        device?.let {
                            if(it.name != null) {
                                // if (it.name.startsWith("Alpha")) {
                                    mAdapter.addData(it)
                                // }
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        //  mBinding.activityHeader.btSearch.visibility = View.VISIBLE
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }
                        if (mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.cancelDiscovery()
                        }
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val paired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        if (paired?.bondState == BluetoothDevice.BOND_BONDED) {
                            dismiss()
                            // mAdapter.notifyDataSetChanged()

                            /*mHeaderBinding.etName.setText(paired.name)
                            mHeaderBinding.etAddr.setText(paired.address)
                            SharedData.setSharedData(mContext, SharedData.PRINTER_NAME, paired.name)
                            SharedData.setSharedData(mContext, SharedData.PRINTER_ADDR, paired.address)*/
                        }
                    }
                }
            }
        }

        val searchFilter = IntentFilter()
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND) //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        mActivity.registerReceiver(mBluetoothReceiver, searchFilter)

        mAdapter = SearchDevicesAdapterV2(context, mActivity)
        mBinding.recyclerview.adapter = mAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        discovery()
        // registerBluetoothReceiver() // 리시버 등록
        // checkPermissionsAndStartDiscovery() // 권한 확인 후 검색 시작
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchDevicesV2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    override fun dismiss() {
        super.dismiss()
        unregisterBluetoothReceiver() // 리시버 해제
    }

    private fun registerBluetoothReceiver() {

    }

    private fun unregisterBluetoothReceiver() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery()
        }
        // BroadcastReceiver 등록해제
        mActivity.unregisterReceiver(mBluetoothReceiver)
    }

    private fun checkPermissionsAndStartDiscovery() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN), 1000)
        } else {
            mBluetoothAdapter.let {
                if (!it.isEnabled) {
                    // 활성화 요청
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    bluetoothOnResultLauncher.launch(intent)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    fun initViews() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        //SettingActivity.searchedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91"))

        // mBluetooth.initBluetooth()

        mBinding.closeBtn.setOnClickListener {
            // dismiss()
            discovery()
        }
    }

    private fun discovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if(mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        } else {
            mAdapter.clear()

            val pairedDevices: Set<BluetoothDevice> = mBluetoothAdapter.bondedDevices

            Log.d("pairedDevices", "pairedDevices ===> ${pairedDevices.size}")
            // 페어링된 기기가 존재하는 경우
            pairedDevices.let {
                it.forEach { device ->
                    mAdapter.addData(device)
                }
            }

            mBluetoothAdapter.startDiscovery()
        }
    }
}