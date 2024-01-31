package kr.co.kimberly.wma.menu.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.MainMenuAdapter
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.custom.GridSpacingItemDecoration
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.ActMainBinding
import kr.co.kimberly.wma.menu.setting.SettingActivity
import kr.co.kimberly.wma.model.MainMenuModel

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActMainBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        val list = ArrayList<MainMenuModel>()
        list.add(MainMenuModel(R.drawable.menu01, getString(R.string.menu01), Define.MENU01))
        list.add(MainMenuModel(R.drawable.menu02, getString(R.string.menu02), Define.MENU02))
        list.add(MainMenuModel(R.drawable.menu03, getString(R.string.menu03), Define.MENU03))
        list.add(MainMenuModel(R.drawable.menu04, getString(R.string.menu04), Define.MENU04))
        list.add(MainMenuModel(R.drawable.menu05, getString(R.string.menu05), Define.MENU05))
        list.add(MainMenuModel(R.drawable.menu06, getString(R.string.menu06), Define.MENU06))
        list.add(MainMenuModel(R.drawable.menu07, getString(R.string.menu07), Define.MENU07))
        list.add(MainMenuModel(R.drawable.menu08, getString(R.string.menu08), Define.MENU08))
        list.add(MainMenuModel(R.drawable.menu09, getString(R.string.menu09), Define.MENU09))

        val adapter = MainMenuAdapter(mContext, mActivity)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = GridLayoutManager(mActivity, 3)
        mBinding.recyclerview.addItemDecoration(GridSpacingItemDecoration(spanCount = 3, spacing = 16f.fromDpToPx()))

        mBinding.settingBtn.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                startActivity(Intent(mContext, SettingActivity::class.java))
            }
        })
    }

    private fun Float.fromDpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}