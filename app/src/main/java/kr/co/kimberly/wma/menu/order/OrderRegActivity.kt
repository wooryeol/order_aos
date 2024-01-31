package kr.co.kimberly.wma.menu.order

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.MainMenuAdapter
import kr.co.kimberly.wma.adapter.OrderRegAdapter
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.model.MainMenuModel
import kr.co.kimberly.wma.model.OrderRegModel

class OrderRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        val list = ArrayList<OrderRegModel>()
        for(i: Int in 1..10) {
            list.add(OrderRegModel("(34870) 하기스프리미어 3공 100/1", "10", "0", "9,999,999원", "240", "9,999,999,999원"))
        }

        val adapter = OrderRegAdapter(mContext, mActivity)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}