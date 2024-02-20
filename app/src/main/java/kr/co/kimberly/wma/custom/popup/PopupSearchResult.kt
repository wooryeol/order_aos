package kr.co.kimberly.wma.custom.popup

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.AccountSearchAdapter
import kr.co.kimberly.wma.adapter.SearchResultAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.PopupSearchResultBinding
import kr.co.kimberly.wma.model.AccountSearchModel
import kr.co.kimberly.wma.model.SearchResultModel
import java.util.ArrayList

class PopupSearchResult(mContext: Context, val list: ArrayList<SearchResultModel>): Dialog(mContext) {
    private lateinit var mBinding: PopupSearchResultBinding

    private var context = mContext

    var onItemSelect: ((SearchResultModel) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupSearchResultBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private fun initViews() {
        // setCancelable(false) // 뒤로가기 버튼, 바깥 화면 터치시 닫히지 않게

        // (중요) Dialog 는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius 가 보이지 않음
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val adapter = SearchResultAdapter(context)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        if(list.size > 10) {
            Utils.dialogResize(context, window)
        }

        adapter.itemClickListener = object: SearchResultAdapter.ItemClickListener {
            override fun onItemClick(item: SearchResultModel) {
                onItemSelect?.invoke(item)
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