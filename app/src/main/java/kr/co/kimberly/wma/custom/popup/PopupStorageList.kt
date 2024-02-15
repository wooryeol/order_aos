package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.selects.select
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.StorageListAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupStorageListBinding
import kr.co.kimberly.wma.menu.inventory.InventoryActivity

class PopupStorageList(private val mContext: AppCompatActivity, private val mActivity: Activity, handler: Handler) {

    private lateinit var mBinding: PopupStorageListBinding
    private val mDialog = Dialog(mContext)
    private val mHandler = handler

    fun show() {
        mBinding = PopupStorageListBinding.inflate(mContext.layoutInflater)

        mDialog.setCancelable(true)
        mDialog.setContentView(mBinding.root)

        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        InventoryActivity.storageList.add("(I001) 기본창고")
        /*InventoryActivity.storageList.add("(I003) 내부창고")
        InventoryActivity.storageList.add("(R001) 반품창고")
        InventoryActivity.storageList.add("(V001) 12호차량")
        InventoryActivity.storageList.add("(V003) 23호차량")
        InventoryActivity.storageList.add("(X001) 외부창고")*/

        if (InventoryActivity.storageList.size > 10) {
            Utils.dialogResize(mContext, mDialog.window)
        }

        val adapter = StorageListAdapter(mContext, mActivity) { data ->
            val message = android.os.Message.obtain()
            message.obj = data
            mHandler.sendMessage(message)
            InventoryActivity.storageList.clear()
            mDialog.dismiss()
        }
        adapter.dataList = InventoryActivity.storageList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)

        mDialog.show()
    }
}