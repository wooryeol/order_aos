package kr.co.kimberly.wma.network.model

class DevicesModel(
    var deviceName: String,
    var deviceAddress: String,
    var uuid: String,
    var isConnected : Boolean = false
)