package kr.co.kimberly.wma.menu.information

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.kimberly.wma.GlobalApplication
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.ConnectThread
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountInformation
import kr.co.kimberly.wma.custom.popup.PopupLoading
import kr.co.kimberly.wma.custom.popup.PopupNotice
import kr.co.kimberly.wma.databinding.ActInformationBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.network.ApiClientService
import kr.co.kimberly.wma.network.model.DataModel
import kr.co.kimberly.wma.network.model.DetailInfoModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.ResultModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import kr.co.kimberly.wma.network.model.SlipOrderListModel
import retrofit2.Call
import retrofit2.Response
import java.util.UUID

class InformationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActInformationBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var radioGroupCheckedListener: OnCheckedChangeListener

    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private var mSearchType: String? = null // 조회 유형
    private var popupInformation : PopupAccountInformation? = null // 정보조회
    private var detailInfoModel: DetailInfoModel? = null // 상세정보
    private var accountName = ""
    private var itemName = ""

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInformationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this
        mLoginInfo = Utils.getLoginData()
        mSearchType = Define.TYPE_CUSTOMER


        setSetting()

        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        mBinding.radioGroup.setOnCheckedChangeListener(radioGroupCheckedListener)

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                if(mBinding.etSearch.text.isEmpty()) {
                    Utils.popupNotice(mContext, getString(R.string.etSearchEmpty))
                } else {
                    getInfo(mBinding.etSearch.text.toString())
                }
            }
        })

        mBinding.phone.setOnClickListener {
            if(mBinding.phone.text.isNotEmpty()) {
                checkPermission(mBinding.phone.text.toString())
            }
        }

        mBinding.inChargeNum.setOnClickListener {
            if(mBinding.inChargeNum.text.isNotEmpty()) {
                checkPermission(mBinding.inChargeNum.text.toString())
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                clearButton()
            }
        })

        mBinding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                mBinding.search.performClick()
                true
            } else {
                false
            }
        }

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
            mBinding.productInfo.isChecked = true
            mBinding.etSearch.hint = getString(R.string.productHint)
            mBinding.productInfoLayout.visibility = View.VISIBLE
            mBinding.accountInfoLayout.visibility = View.GONE
            mSearchType = Define.TYPE_ITEM
            getDetailInfo(it, Define.BARCODE)
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
    @SuppressLint("MissingPermission")
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
        //헤더 설정
        mBinding.header.headerTitle.text = getString(R.string.menu09)

        radioGroupCheckedListener = OnCheckedChangeListener { _, checkedId ->
            hideKeyboard()
            when(checkedId) {
                R.id.accountInfo -> {
                    if (accountName.isNotEmpty()){
                        mBinding.tvProductName.text = accountName
                        mBinding.tvProductName.visibility = View.VISIBLE
                        mBinding.btProductNameEmpty.visibility = View.VISIBLE
                        mBinding.etSearch.visibility = View.GONE
                    } else {
                        mBinding.etSearch.hint = getString(R.string.accountHint)
                        mBinding.etSearch.visibility = View.VISIBLE
                        mBinding.tvProductName.visibility = View.GONE
                        mBinding.btProductNameEmpty.visibility = View.GONE
                    }

                    mBinding.accountInfoLayout.visibility = View.VISIBLE
                    mBinding.productInfoLayout.visibility = View.GONE
                    mSearchType = Define.TYPE_CUSTOMER
                }
                R.id.productInfo -> {
                    if (itemName.isNotEmpty()){
                        mBinding.tvProductName.text = itemName
                        mBinding.tvProductName.visibility = View.VISIBLE
                        mBinding.btProductNameEmpty.visibility = View.VISIBLE
                        mBinding.etSearch.visibility = View.GONE
                    } else {
                        mBinding.etSearch.visibility = View.VISIBLE
                        mBinding.tvProductName.visibility = View.GONE
                        mBinding.btProductNameEmpty.visibility = View.GONE
                    }
                    mBinding.etSearch.hint = getString(R.string.productHint)
                    mBinding.productInfoLayout.visibility = View.VISIBLE
                    mBinding.accountInfoLayout.visibility = View.GONE
                    mSearchType = Define.TYPE_ITEM
                }
            }
        }
    }

    fun clearButton() {
        mBinding.etSearch.text = null
        if (mBinding.tvProductName.text == accountName) {
            accountName = ""
        }
        if (mBinding.tvProductName.text == itemName) {
            itemName = ""
        }
        when(mSearchType){
            Define.TYPE_CUSTOMER -> {
                mBinding.etSearch.hint = mContext.getString(R.string.accountHint)

            }
            Define.TYPE_ITEM -> {
                mBinding.etSearch.hint = mContext.getString(R.string.productNameHint)
            }
        }
        mBinding.etSearch.visibility = View.VISIBLE
        mBinding.tvProductName.text = null
        mBinding.tvProductName.visibility = View.GONE
        mBinding.btProductNameEmpty.visibility = View.GONE
        GlobalApplication.showKeyboard(mContext, mBinding.etSearch)
    }

    private fun getInfo(searchCondition: String) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.masterInfo(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, mSearchType!!, searchCondition)
        //test
        //val call = service.masterInfo("C000000", "mb2004", mSearchType!!, searchCondition)


        call.enqueue(object : retrofit2.Callback<ResultModel<DataModel<Any>>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ResultModel<DataModel<Any>>>,
                response: Response<ResultModel<DataModel<Any>>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        Utils.log("item ====> $item")
                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            val gson = Gson()
                            when(mSearchType) {
                                Define.TYPE_CUSTOMER -> {
                                    Utils.log("customer info search success ====> ${Gson().toJson(item.data.customerList)}")

                                    // customerList를 JSON 문자열로 변환 후 다시 List<Customer>로 변환
                                    val jsonElement = gson.toJsonTree(item.data.customerList)
                                    val jsonString = gson.toJson(jsonElement)
                                    val customerListType = object : TypeToken<ArrayList<SlipOrderListModel>>() {}.type
                                    val customerList: ArrayList<SlipOrderListModel> = gson.fromJson(jsonString, customerListType)

                                    popupInformation = PopupAccountInformation(mContext, customerList, null  )
                                    popupInformation?.onAccountSelect = {
                                        mBinding.tvProductName.text = it.customerNm
                                        accountName = it.customerNm.toString()
                                        getDetailInfo(it.customerCd.toString())
                                    }
                                    popupInformation?.show()
                                }

                                Define.TYPE_ITEM -> {
                                    Utils.log("item info search success ====> ${Gson().toJson(item.data.itemList)}")
                                    // itemList를 JSON 문자열로 변환 후 다시 List<Customer>로 변환
                                    val jsonElement = gson.toJsonTree(item.data.itemList)
                                    val jsonString = gson.toJson(jsonElement)
                                    val itemListType = object : TypeToken<ArrayList<SearchItemModel>>() {}.type
                                    val itemList: ArrayList<SearchItemModel> = gson.fromJson(jsonString, itemListType)

                                    val popupAccountInformation = PopupAccountInformation(mContext, null, itemList)
                                    popupAccountInformation.onItemSelect = {
                                        itemName = it.itemNm.toString()
                                        getDetailInfo(it.itemCd.toString(), Define.SEARCH)
                                    }
                                    popupAccountInformation.show()
                                }

                                else -> {
                                    Utils.popupNotice(mContext, item.returnMsg, mBinding.etSearch)
                                }
                            }
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg, mBinding.etSearch)
                        }
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DataModel<Any>>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("getInfo failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }


    // 상세 정보 조회
    private fun getDetailInfo(searchCd: String, subSearchType: String? = null) {
        val loading = PopupLoading(mContext)
        loading.show()
        val service = ApiClientService.retrofit.create(ApiClientService::class.java)
        val call = service.masterInfoDetail(mLoginInfo.agencyCd!!, mLoginInfo.userId!!, mSearchType!!, subSearchType , searchCd)

        //test
        //val call = service.masterInfoDetail("C000000", "mb2004", mSearchType!!, searchCd)

        //val call = service.masterInfoDetail("C000028", "mb2004", "C", "000012")

        call.enqueue(object : retrofit2.Callback<ResultModel<DetailInfoModel>> {
            override fun onResponse(
                call: Call<ResultModel<DetailInfoModel>>,
                response: Response<ResultModel<DetailInfoModel>>
            ) {
                loading.hideDialog()
                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        Utils.log("item ====> $item")
                        if (item.returnCd == Define.RETURN_CD_90 || item.returnCd == Define.RETURN_CD_91 || item.returnCd == Define.RETURN_CD_00) {
                            val data = item.data
                            when(mSearchType) {
                                Define.TYPE_CUSTOMER -> {
                                    Utils.log("customer detail info search success ====> ${Gson().toJson(item.data)}")

                                    detailInfoModel = item.data.copy(
                                        searchType = data.searchType!!,
                                        customerCd = data.customerCd!!,
                                        customerNm = data.customerNm!!,
                                        representNm = data.representNm!!,
                                        bizNo = data.bizNo!!,
                                        telNo = data.telNo!!,
                                        faxNo = data.faxNo!!,
                                        address = data.address!!,
                                        billingVendor = data.billingVendor!!,
                                        storeSize = data.storeSize!!,
                                        buyEmpNm = data.buyEmpNm!!,
                                        buyEmpMobileNo = data.buyEmpMobileNo!!
                                    )
                                    setInfo(detailInfoModel!!)

                                }

                                Define.TYPE_ITEM -> {
                                    Utils.log("item detail info search success ====> ${Gson().toJson(item.data)}")

                                    detailInfoModel = item.data.copy(
                                        representNm =  data.resultType!!,
                                        makerNm = data.makerNm!!,
                                        itemCd = data.itemCd!!,
                                        itemNm = data.itemNm!!,
                                        kanCode = data.kanCode!!,
                                        dimension = data.dimension!!,
                                        getBoxQty = data.getBoxQty!!,
                                        vatType = data.vatType!!,
                                        registerImgYn = data.registerImgYn!!,
                                        imgUrl = data.imgUrl!!,
                                    )
                                    mBinding.tvProductName.text = detailInfoModel?.itemNm
                                    setInfo(detailInfoModel!!)

                                }

                                else -> {
                                    Utils.popupNotice(mContext, item.returnMsg, mBinding.etSearch)
                                }
                            }
                        } else {
                            Utils.popupNotice(mContext, item.returnMsg, mBinding.etSearch)
                        }
                    }
                } else {
                    Utils.log("${response.code()} ====> ${response.message()}")
                    Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
                }
            }

            override fun onFailure(call: Call<ResultModel<DetailInfoModel>>, t: Throwable) {
                loading.hideDialog()
                Utils.log("getInfo failed ====> ${t.message}")
                Utils.popupNotice(mContext, "잠시 후 다시 시도해주세요")
            }

        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setInfo(detailInfoModel: DetailInfoModel){
        /*mBinding.etSearch.visibility = View.GONE
        mBinding.tvProductName.visibility = View.VISIBLE
        mBinding.btProductNameEmpty.visibility = View.VISIBLE*/
        when(mSearchType) {
            Define.TYPE_CUSTOMER -> {
                if (accountName.isNotEmpty()) {
                    mBinding.tvProductName.text = accountName
                    mBinding.etSearch.visibility = View.GONE
                    mBinding.tvProductName.visibility = View.VISIBLE
                    mBinding.btProductNameEmpty.visibility = View.VISIBLE
                } else {
                    mBinding.etSearch.visibility = View.VISIBLE
                    mBinding.tvProductName.visibility = View.GONE
                    mBinding.btProductNameEmpty.visibility = View.GONE
                }
                mBinding.accountCode.text = detailInfoModel.customerCd
                mBinding.account.text = detailInfoModel.customerNm
                mBinding.represent.text = detailInfoModel.representNm
                mBinding.businessNum.text = detailInfoModel.bizNo
                mBinding.phone.text = detailInfoModel.telNo
                if (detailInfoModel.telNo != "-"){
                    mBinding.phone.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
                mBinding.fax.text = detailInfoModel.faxNo
                mBinding.address.text = detailInfoModel.address
                mBinding.customer.text = detailInfoModel.billingVendor
                mBinding.scale.text = detailInfoModel.storeSize
                mBinding.inCharge.text = detailInfoModel.buyEmpNm
                mBinding.inChargeNum.text = detailInfoModel.buyEmpMobileNo
                if (detailInfoModel.buyEmpMobileNo != "-"){
                    mBinding.inChargeNum.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                }
            }
            Define.TYPE_ITEM -> {
                if (itemName.isNotEmpty()) {
                    mBinding.tvProductName.text = itemName
                    mBinding.etSearch.visibility = View.GONE
                    mBinding.tvProductName.visibility = View.VISIBLE
                    mBinding.btProductNameEmpty.visibility = View.VISIBLE
                } else {
                    mBinding.etSearch.visibility = View.VISIBLE
                    mBinding.tvProductName.visibility = View.GONE
                    mBinding.btProductNameEmpty.visibility = View.GONE
                }
                mBinding.manufacturer.text = detailInfoModel.makerNm
                mBinding.productCode.text = detailInfoModel.itemCd
                mBinding.productName.text = detailInfoModel.itemNm
                mBinding.barcode.text = detailInfoModel.kanCode
                mBinding.incomeQty.text = Utils.decimal(detailInfoModel.getBoxQty!!)
                mBinding.Dimension.text = detailInfoModel.dimension
                mBinding.Dimension.isSelected = true
                mBinding.tax.text = detailInfoModel.vatType
                if (detailInfoModel.registerImgYn == "Y") {
                    mBinding.noImage.visibility = View.GONE
                    mBinding.image.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(detailInfoModel.imgUrl) // 불러올 이미지 url
                        //.placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                        //.error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                        //.fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                        //.circleCrop() // 동그랗게 자르기
                        .into(mBinding.image) // 이미지를 넣을 뷰
                } else {
                    mBinding.noImage.visibility = View.VISIBLE
                    mBinding.image.visibility = View.GONE
                }

            }
        }
    }

    private fun hideKeyboard() {
        mBinding.etSearch.setText("")
        mBinding.etSearch.clearFocus()

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mBinding.etSearch.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun checkPermission(number: String){
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    call(number)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            })
            .setDeniedMessage("권한을 허용해주세요.\n[설정] > [애플리케이션] > [앱 권한]")
            .setPermissions(Manifest.permission.CALL_PHONE)
            .check()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun call(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${number}")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}