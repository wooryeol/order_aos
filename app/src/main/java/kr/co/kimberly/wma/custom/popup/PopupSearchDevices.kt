package kr.co.kimberly.wma.custom.popup

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.SearchDevicesAdapter
import kr.co.kimberly.wma.databinding.PopupSearchDevicesBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.DevicesModel

class PopupSearchDevices(private val mContext: AppCompatActivity, private val mActivity: Activity) {

    private lateinit var mBinding: PopupSearchDevicesBinding
    private val mDialog = Dialog(mContext)

    fun show() {
        mBinding = PopupSearchDevicesBinding.inflate(mContext.layoutInflater)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setContentView(mBinding.root)
        mDialog.setCancelable(false)

        SettingActivity.searchedList.add(DevicesModel("KDC200[02070260]", "00:19:01:31:4E:91", false))

        val adapter = SearchDevicesAdapter(mContext, mActivity)
        adapter.dataList = SettingActivity.searchedList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
        /*mBinding.recyclerview.addItemDecoration(GridSpacingItemDecoration(spanCount = 3, spacing = 16f.fromDpToPx()))*/

        mBinding.closeBtn.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.show()
        mDialog.window?.setLayout(960, 1344)
    }

    private fun Float.fromDpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}