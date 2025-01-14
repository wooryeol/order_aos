package kr.co.kimberly.wma.menu.purchase

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.PurchaseRequestAdapter
import kr.co.kimberly.wma.common.ConnectThread
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupDoubleMessage
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupNoticeV2
import kr.co.kimberly.wma.databinding.ActPurchaseRequestBinding
import kr.co.kimberly.wma.db.DBHelper
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.SapModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.util.UUID

@SuppressLint("MissingPermission", "SetTextI18n")
class PurchaseRequestActivity: AppCompatActivity() {
    private lateinit var mBinding: ActPurchaseRequestBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel

    private var totalAmount: Long = 0
    var purchaseAdapter: PurchaseRequestAdapter? = null
    private var thread : ConnectThread? = null

    private val db : DBHelper by lazy {
        DBHelper.getInstance(applicationContext)
    }
    private var isSave = true // 액티비티가 종료 될 때 이 값을 통해 저장 여부 선택

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActPurchaseRequestBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()

        mBinding.header.headerTitle.text = getString(R.string.menu08)
        mBinding.bottom.bottomButton.text = getString(R.string.menu08)

        setAdapter()

        // 소프트키 뒤로가기
        this.onBackPressedDispatcher.addCallback(this, callback)

        // 헤더 뒤로가기
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                goBack()
            }
        })

        mBinding.bottom.bottomButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupDoubleMessage = PopupDoubleMessage(mContext, "발주전송", "SAP Name : ${purchaseAdapter?.selectedSAP?.sapCustomerNm}\n총금액 : ${Utils.decimalLong(totalAmount)}원", getString(R.string.purchasePostMsg03), true)
                if (purchaseAdapter?.itemList!!.isEmpty()) {
                    Utils.popupNotice(mContext, "제품이 등록되지 않았습니다.")
                } else {
                    popupDoubleMessage.itemClickListener = object: PopupDoubleMessage.ItemClickListener {
                        override fun onCancelClick() {
                            Utils.log("취소 클릭")
                        }

                        override fun onOkClick() {
                            postOrderSlip()
                        }
                    }
                    popupDoubleMessage.show()
                }
            }
        })

        mBinding.header.scanBtn.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val isScannerConnected = SharedData.getSharedData(mContext, "isScannerConnected", false)
                // 사용 여부 확인
                if (!isScannerConnected) {
                    val popupNotice = PopupNotice(mContext, mContext.getString(R.string.msg_scan_connect_error))
                    popupNotice.itemClickListener = object : PopupNotice.ItemClickListener{
                        override fun onOkClick() {
                            val intent = Intent(mContext, SettingActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    popupNotice.show()
                    return
                }
                if (thread != null) {
                    thread?.cancel()
                    thread = null
                    mBinding.header.scanBtn.setColorFilter(getColor(R.color.trans))
                } else {
                    checkScanner()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        purchaseAdapter?.cleanup()
        thread?.cancel()
    }

    override fun onPause() {
        super.onPause()
        thread?.cancel()
    }

    override fun onResume() {
        super.onResume()
        checkScanner()
    }
    private fun checkScanner(){
        val isScannerConnected = SharedData.getSharedData(mContext, "isScannerConnected", false)
        if (isScannerConnected) {
            mBinding.header.scanBtn.setColorFilter(getColor(R.color.black))
            val scanner = SharedData.getSharedData(mContext, SharedData.SCANNER_ADDR, "")
            if (scanner.isNotEmpty()){
                Utils.toast(mContext, "기기를 연결 중입니다.")
                connectDevice(scanner)
            }
        }
    }

    // 디바이스에 연결
    private fun connectDevice(deviceAddress: String) {
        val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.let { adapter ->
            // 기기 검색을 수행중이라면 취소
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }

            // 서버의 역할을 수행 할 Device 획득
            val device = adapter.getRemoteDevice(deviceAddress)
            // UUID 선언
            val uuid = UUID.fromString(Define.UUID)
            try {
                GlobalScope.launch(Dispatchers.IO) {
                    thread = ConnectThread(uuid, device, mContext)
                    thread?.run()
                }
                Utils.toast(mContext,"${device.name}과 연결되었습니다.")
            } catch (e: Exception) { // 연결에 실패할 경우 호출됨
                Utils.log("스캐너의 전원이 꺼져 있습니다. 기기를 확인해주세요.")
                return
            }
        }
    }



    // 어댑터 세팅
    @SuppressLint("SetTextI18n")
    private fun setAdapter(){
        val list = if (db.purchaseList != emptyArray<SearchItemModel>()) { db.purchaseList as ArrayList<SearchItemModel>} else {
            arrayListOf()}
        val data: SapModel = intent.getSerializableExtra("purchaseSapModel") as? SapModel ?: SapModel()

        purchaseAdapter = PurchaseRequestAdapter(mContext, mActivity, list, data) { itemList, item ->
            totalAmount = 0
            itemList.map {
                val stringWithoutComma = it.amount.toString().replace(",", "")
                totalAmount += stringWithoutComma.toLong()
            }

            val formatTotalMoney = Utils.decimalLong(totalAmount)
            mBinding.tvTotalAmount.text = "${formatTotalMoney}원"
        }

        mBinding.recyclerview.adapter = purchaseAdapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (list.isNotEmpty()) {
            list.forEach {
                totalAmount += it.amount!!
            }
            mBinding.tvTotalAmount.text = "${Utils.decimalLong(totalAmount)}원"
        }
    }

    private fun postOrderSlip() {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val sapModel = purchaseAdapter?.selectedSAP

        val agencyCd = mLoginInfo.agencyCd
        val userId = mLoginInfo.userId
        val sapCustomerCd = sapModel?.sapCustomerCd
        val arriveCd = sapModel?.arriveCd
        val slipType = Define.ORDER
        val orderDate = Utils.getCurrentDateFormatted()
        val deliveryDate = Utils.getCurrentDateFormatted()
        val totalAmount = totalAmount
        val jsonArray = Gson().toJsonTree(purchaseAdapter?.itemList).asJsonArray

        val json = JsonObject().apply {
            addProperty("agencyCd", agencyCd)
            //test
            //addProperty("agencyCd", "C000032")
            addProperty("userId", userId)
            addProperty("sapCustomerCd", sapCustomerCd)
            addProperty("arriveCd", arriveCd)
            addProperty("slipType", slipType)
            addProperty("orderDate", orderDate)
            addProperty("orderDate", orderDate)
            addProperty("deliveryDate", deliveryDate)
            addProperty("totalAmount", totalAmount)
        }
        json.add("orderInfo", jsonArray)

        val obj = json.toString()
        val body = obj.toRequestBody("application/json".toMediaTypeOrNull())
        val call = service.headOfficeOrderSlip(body)
        Utils.log("purchase request body ====> ${Gson().toJson(json)}")

        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<Unit>>> {
            override fun onResponse(
                call: Call<ResultModel<DataModel<Unit>>>,
                response: Response<ResultModel<DataModel<Unit>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00) {
                        Utils.log("purchase success")
                        Utils.log("item ====> ${item.data}")
                        Utils.log("order success ====> ${Gson().toJson(item)}")
                        Utils.toast(mContext, "주문이 전송되었습니다.")

                        // 주문이 전송되면 데이터 초기화
                        deleteData()

                        val intent = Intent(mContext, PurchaseApprovalActivity::class.java).apply {
                            putExtra("slipNo", item.data?.slipNo)
                            putExtra("sapModel", sapModel)
                            putExtra("purchaseList", purchaseAdapter?.itemList)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DataModel<Unit>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("purchase failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    private fun saveData() {
        SharedData.setSharedDataModel(mContext, "purchaseSapModel", purchaseAdapter?.selectedSAP!!)
        db.deletePurchaseData()
        purchaseAdapter?.itemList?.forEach {
            db.insertPurchaseData(it)
        }
    }

    private fun deleteData() {
        SharedData.setSharedData(mContext, "purchaseSapModel", "")
        db.deletePurchaseData()
        isSave = false
    }

    override fun onStop() {
        super.onStop()
        if (!purchaseAdapter?.itemList.isNullOrEmpty() && isSave) {
            saveData()
        }
    }

    // 뒤로가기 버튼
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }

    private fun goBack() {
        // 주문 도중 나갈 경우
        if (!purchaseAdapter?.itemList.isNullOrEmpty()) {
            PopupNoticeV2(mContext, "기존 주문이 완료되지 않았습니다.\n전표를 저장하시겠습니까?",
                object : Handler(Looper.getMainLooper()) {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun handleMessage(msg: Message) {
                        when (msg.what) {
                            Define.EVENT_OK -> {
                                saveData()
                                finish()
                            }
                            Define.EVENT_CANCEL -> {
                                deleteData()
                                finish()
                            }
                        }
                    }
                }
            ).show()
        } else {
            deleteData()
            finish()
        }
    }
}