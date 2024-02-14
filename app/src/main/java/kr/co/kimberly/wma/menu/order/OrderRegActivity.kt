package kr.co.kimberly.wma.menu.order

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.adapter.OrderRegAdapter
import kr.co.kimberly.wma.databinding.ActOrderRegBinding
import kr.co.kimberly.wma.databinding.HeaderOrderRegBinding
import kr.co.kimberly.wma.model.OrderRegModel

class OrderRegActivity : AppCompatActivity() {
    private lateinit var mBinding: ActOrderRegBinding
    private lateinit var mHeaderBinding: HeaderOrderRegBinding
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActOrderRegBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mContext = this
        mActivity = this

        mBinding.header.headerTitle.text = getString(R.string.menu01)
        mBinding.bottom.bottomButton.text = getString(R.string.orderApproval)

        mBinding.header.backBtn.setOnClickListener {
            finish()
        }

        val list = ArrayList<OrderRegModel>()
        for(i: Int in 1..10) {
            list.add(OrderRegModel("(34870) 하기스프리미어 3공 100/1", "10", "0", "9,999,999원", "240", "9,999,999,999원"))
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mHeaderBinding = HeaderOrderRegBinding.inflate(inflater, null, false)

        val adapter = OrderRegAdapter(mContext, mActivity)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(mContext)
    }
}