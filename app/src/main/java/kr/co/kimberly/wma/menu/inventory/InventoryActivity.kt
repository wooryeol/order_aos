package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
import kr.co.kimberly.wma.common.ConnectThread
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.custom.popup.PopupWarehouseList
import kr.co.kimberly.wma.databinding.ActInventoryBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.WarehouseListModel
import kr.co.kimberly.wma.network.model.WarehouseStockModel
import retrofit2.Call
import retrofit2.Response
import java.util.UUID

@SuppressLint("MissingPermission")
class InventoryActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInventoryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private lateinit var agencyCd : String // 대리점 코드
    private lateinit var userId : String // 사용자 아이디
    private var warehouseCd: String? = null // 창고 코드
    private var itemList: ArrayList<WarehouseStockModel>? = null
    private var adapter : InventoryListAdapter? = null
    private var thread : ConnectThread? = null // 스캐너 연결
    var onItemScan: ((String) -> Unit)? = null // 제품 삭제 시
    private var barcodeReceiver = object : BroadcastReceiver() { // 스캐너 값 읽어오는 부분
        override fun onReceive(context: Context, intent: Intent?) {
            when (val barcode = intent?.getStringExtra("data")) {
                null -> {
                    // 데이터가 null일 때 아무것도 하지 않음
                    Utils.popupNotice(context, "바코드를 다시 스캔해주세요")
                }
                else -> {
                    if (barcode.isNotEmpty()) {
                        Utils.log("adapter barcode data ====> $barcode")
                        onItemScan?.invoke(barcode)
                    }
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInventoryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()
        agencyCd =  mLoginInfo.agencyCd!!
        userId = mLoginInfo.userId!!

        // 초기 셋팅
        setSetting()

        // 헤더 설정 변경
        mBinding.header.headerTitle.text = getString(R.string.menu06)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.etProductName.addTextChangedListener {
            if (mBinding.etProductName.text.isNullOrEmpty()) {
                mBinding.btProductNameEmpty.visibility = View.GONE
            } else {
                mBinding.btProductNameEmpty.visibility = View.VISIBLE
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.btProductNameEmpty.visibility = View.GONE
                mBinding.tvProductName.text = null
                mBinding.tvProductName.visibility = View.GONE
                mBinding.etProductName.text = null
                mBinding.etProductName.visibility = View.VISIBLE

            }

        })

        mBinding.etProductName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                mBinding.search.performClick()
                true
            } else {
                false
            }
        }

        // 아이템 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.etProductName.text.isNullOrEmpty()) {
                    Utils.popupNotice(mContext, getString(R.string.productNameHint))
                } else if(mBinding.tvBranchHouse.text.isNullOrEmpty()) {
                    Utils.popupNotice(mContext, getString(R.string.branchHouseHint))
                } else {
                    warehouseStock(mBinding.etProductName.text.toString(), Define.SEARCH)
                }
            }
        })

        // 창고 선택
        mBinding.tvBranchHouse.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                warehouseList()
            }
        })

        // 제품 삭제
        mBinding.btProductNameEmpty.setOnClickListener(object :OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                mBinding.etProductName.text = null
                mBinding.tvProductName.text = null
                mBinding.tvProductName.visibility = View.GONE
                mBinding.etProductName.visibility = View.VISIBLE
                mBinding.btProductNameEmpty.visibility = View.GONE
                mBinding.etProductName.hint = v.context.getString(R.string.productNameHint)
                mBinding.noSearch.visibility = View.VISIBLE
                mBinding.recyclerview.visibility = View.GONE
                itemList?.clear()
                adapter?.notifyDataSetChanged()

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

        // 아이템 바코드 스캔
        onItemScan = {
            //warehouseStock(it, Define.BARCODE)
            warehouseStock("8801166011747", Define.BARCODE)
        }

        val filter = IntentFilter("kr.co.kimberly.wma.ACTION_BARCODE_SCANNED")
        mContext.registerReceiver(barcodeReceiver, filter, RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
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

    // 디바이스에 연결
    private fun connectDevice(deviceAddress: String) {
        val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        /**
         * 기기의 UUID를 가져와야 할 떄 사용하는 코드
         **/

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

    private fun checkScanner(){
        val isScannerConnected = SharedData.getSharedData(mContext, "isScannerConnected", false)
        if (isScannerConnected) {
            mBinding.header.scanBtn.setColorFilter(getColor(R.color.black))
            val scanner = SharedData.getSharedData(mContext, SharedData.SCANNER_ADDR, "")
            if (scanner.isNotBlank()){
                Utils.toast(mContext, "기기를 연결 중입니다.")
                connectDevice(scanner)
            }
        }
    }

    private fun setSetting() {
        // 텍스트를 흘러가게 하기 위함
        mBinding.tvBranchHouse.isSelected = true

        // 진입 시 창고 리스트 팝업 노출
        warehouseList()
    }

    // 검색을 눌렀을 때
    private fun showInventoryList(list: ArrayList<WarehouseStockModel>) {
        adapter = InventoryListAdapter(mContext, mActivity)
        adapter!!.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (list.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE

            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(mBinding.etProductName.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            mBinding.etProductName.clearFocus()
        } else {
            Utils.popupNotice(mContext, getString(R.string.searchNothing))
        }
    }

    private fun warehouseList(){
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.warehouseList(agencyCd, userId)

        //test
        //val call = service.warehouseList("C000028", "mb2004")

        call.enqueue(object : retrofit2.Callback<ResultModel<List<WarehouseListModel>>> {
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onResponse(
                call: Call<ResultModel<List<WarehouseListModel>>>,
                response: Response<ResultModel<List<WarehouseListModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("warehouse search success ====> ${Gson().toJson(item)}")
                        val list = item.data as ArrayList<WarehouseListModel>
                        val popupWarehouseList = PopupWarehouseList(mContext, list)
                        popupWarehouseList.onItemSelect = {
                            warehouseCd = it.warehouseCd
                            mBinding.tvBranchHouse.text = "(${it.warehouseCd}) ${it.warehouseNm}"

                            if (!itemList.isNullOrEmpty()) {
                                mBinding.etProductName.text = null
                                mBinding.tvProductName.text = null
                                mBinding.tvProductName.visibility = View.GONE
                                mBinding.etProductName.visibility = View.VISIBLE
                                mBinding.btProductNameEmpty.visibility = View.GONE
                                mBinding.etProductName.hint = mContext.getString(R.string.productNameHint)
                                mBinding.noSearch.visibility = View.VISIBLE
                                itemList?.clear()
                                adapter?.notifyDataSetChanged()
                            }
                        }
                        popupWarehouseList.show()
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<List<WarehouseListModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("warehouse search failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    // 검색 아이템 리스트 조회
    fun warehouseStock(searchCondition: String, searchType: String) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.warehouseStock(agencyCd, userId, warehouseCd!!, searchType, searchCondition)
        //test
        //val call = service.warehouseStock("C000028", "mb2004", "I001", "하기스")

        call.enqueue(object : retrofit2.Callback<ResultModel<List<WarehouseStockModel>>> {
            override fun onResponse(
                call: Call<ResultModel<List<WarehouseStockModel>>>,
                response: Response<ResultModel<List<WarehouseStockModel>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item?.returnCd == Define.RETURN_CD_00 || item?.returnCd == Define.RETURN_CD_90 || item?.returnCd == Define.RETURN_CD_91) {
                        Utils.log("stock search success ====> ${Gson().toJson(item.data)}")
                        itemList = item.data as ArrayList<WarehouseStockModel>
                        showInventoryList(itemList!!)

                        mBinding.etProductName.visibility = View.GONE
                        mBinding.tvProductName.text = searchCondition
                        mBinding.tvProductName.visibility = View.VISIBLE
                        mBinding.btProductNameEmpty.visibility = View.VISIBLE
                    } else {
                        Utils.popupNotice(mContext, item?.returnMsg!!, mBinding.etProductName)
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<List<WarehouseStockModel>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("stock failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }
}