package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.AccountSearchAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupSearchResultBinding
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.LoginResponseModel
import kr.co.kimberly.wma.network.model.SearchItemModel
import kr.co.kimberly.wma.network.model.SlipOrderListModel

@SuppressLint("NotifyDataSetChanged")
class PopupAccountInformation(mContext: Context, val accountList: ArrayList<CustomerModel>? = null, val itemList: ArrayList<SearchItemModel>? = null): Dialog(mContext) {
    private lateinit var mBinding: PopupSearchResultBinding
    private lateinit var mLoginInfo: LoginResponseModel // 로그인 정보
    private var context = mContext

    var onItemSelect: ((ArrayList<SlipOrderListModel>) -> Unit)? = null
    var onTitleSelect: ((CustomerModel) -> Unit)? = null
    var adapter: AccountSearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchResultBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private fun initViews() {
        mLoginInfo = Utils.getLoginData()!!

        // setCancelable(false) // 뒤로가기 버튼, 바깥 화면 터치시 닫히지 않게

        // (중요) Dialog 는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius 가 보이지 않음
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        if(accountList?.size!! > 6) {
            Utils.dialogResize(context, window)
        }

        adapter = AccountSearchAdapter(context)
        //adapter?.dataList = dataList
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        adapter?.itemClickListener = object: AccountSearchAdapter.ItemClickListener {
            override fun onItemClick(item: CustomerModel) {
                onTitleSelect?.invoke(item)
                hideDialog()
            }
        }
    }

    fun hideDialog() {
        if (isShowing) {
            dismiss()
        }
    }


}