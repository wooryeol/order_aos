package kr.co.kimberly.wma.common

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kr.co.kimberly.wma.network.model.DevicesModel

@SuppressLint("MissingPermission")
class Bluetooth(context: Context, private val searchedList: (ArrayList<DevicesModel>) -> Unit) {
    interface BluetoothListener {
        fun hideLoadingImage()
        fun showLoadingImage()
        fun onChangeAdapterData()
    }

    private val mContext: Context = context
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
     var bluetoothListener: BluetoothListener? = null

    private val scanList = ArrayList<DevicesModel>()

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.name != null) {
                var uuid = "null"

                if(result.scanRecord?.serviceUuids != null) {
                    uuid = result.scanRecord!!.serviceUuids.toString()
                }
                val scanItem = DevicesModel(
                    result.device.name?: "null",
                    uuid,
                    result.device.address?: "null",
                    false
                )
                Utils.log("scanItem ====> ${scanItem.deviceName}")

                if (scanItem.deviceName.startsWith("Alpha") || scanItem.deviceName.contains("KDC")){
                    if(!scanList.contains(scanItem)) {

                        scanList.add(scanItem)
                        updateData()
                    }
                }

                /*if(!scanList.contains(scanItem)) {
                    Utils.log("scanItem ====> $scanItem")
                    scanList.add(scanItem)
                    //updateData()
                }*/
            }
        }

        override fun onScanFailed(errorCode: Int) {
            println("onScanFailed  $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        // GATT의 연결 상태 변경을 감지하는 콜백
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            // 연결이 성공적으로 이루어진 경우
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // GATT 서버에서 사용 가능한 서비스들을 비동기적으로 탐색
                Utils.log("연결 성공")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 연결 끊김
                Utils.log("연결 해제")
            }
        }

        // 장치에 대한 새로운 서비스가 발견되었을 때 호출되는 콜백
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            // 원격 장치가 성공적으로 탐색된 경우
            if(status == BluetoothGatt.GATT_SUCCESS) {
                MainScope().launch {
                    Toast.makeText(context, " ${gatt?.device?.name} 연결 성공", Toast.LENGTH_SHORT).show()
                }.cancel()
            }
        }
    }

    fun startScan(){
        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        bluetoothListener?.showLoadingImage()
    }

    fun stopScan(){
        bluetoothLeScanner.stopScan(scanCallback)
        bluetoothListener?.hideLoadingImage()
        Utils.log("scanList ====> ${Gson().toJson(scanList)}")
    }

    fun connectDevice(deviceData: DevicesModel){
        bluetoothAdapter
            .getRemoteDevice(deviceData.deviceAddress)
            .connectGatt(mContext, false, gattCallback)
    }

    private fun updateData(){
        searchedList(scanList)
    }
}