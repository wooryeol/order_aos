package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.custom.popup.PopupAccountSearch
import kr.co.kimberly.wma.custom.popup.PopupError
import kr.co.kimberly.wma.custom.popup.PopupStorageList
import kr.co.kimberly.wma.databinding.ActInventoryBinding
import kr.co.kimberly.wma.model.InventoryModel

class InventoryActivity : AppCompatActivity() {

    private lateinit var mBinding: ActInventoryBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    private val inventoryList = ArrayList<InventoryModel>()

    companion object {
        val storageList = ArrayList<String>()
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val value = msg.obj as String
            handleValueFromDialog(value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActInventoryBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this


        // 헤더 설정 변경
        mBinding.header.headerTitle.text = getString(R.string.menu06)
        mBinding.header.scanBtn.setImageResource(R.drawable.adf_scanner)
        mBinding.header.backBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                finish()
            }
        })

        // 거래처 검색
        mBinding.search.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                val popupAccountSearch = PopupAccountSearch(mContext)
                popupAccountSearch.onItemSelect = {
                    mBinding.accountName.text = it.name
                }
                popupAccountSearch.show()
            }
        })

        mBinding.search.setOnClickListener {
            showInventoryList()
        }

        // 진입 시 창고 리스트 팝업 활성화
        val dlg = PopupStorageList(this, mActivity, handler)
        dlg.show()
        mBinding.selectStorageBtn.setOnClickListener {
            dlg.show()
        }
    }

    // 검색을 눌렀을 때
    private fun showInventoryList() {
        inventoryList.add(InventoryModel("(00234)크리넥스 수앤수 10매*3", "20", "0", "1000"))

        val adapter = InventoryListAdapter(mContext, mActivity)
        adapter.dataList = inventoryList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        if (inventoryList.isNotEmpty()){
            mBinding.noSearch.visibility = View.GONE
            mBinding.recyclerview.visibility = View.VISIBLE
        } else {
            val dlg = PopupError(this, mActivity)
            dlg.show()
        }
    }

    private fun handleValueFromDialog(value: String) {
        mBinding.selectStorageBtn.text = value
    }
}