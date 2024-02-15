package kr.co.kimberly.wma.menu.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.InventoryListAdapter
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

        mBinding.search.setOnClickListener {
            showInventoryList()
        }

        mBinding.selectStorageBtn.setOnClickListener {
            val dlg = PopupStorageList(this, mActivity, handler)
            dlg.show()
        }
    }

    // 검색을 눌렀을 때
    private fun showInventoryList() {
        //inventoryList.add(InventoryModel("(00234)크리넥스 수앤수 10매*3", "20", "0", "1000"))

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
            mBinding.noSearch.visibility = View.VISIBLE
            mBinding.recyclerview.visibility = View.GONE
        }
    }

    private fun handleValueFromDialog(value: String) {
        mBinding.selectStorageBtn.text = value
    }
}