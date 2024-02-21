package kr.co.kimberly.wma.custom.popup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.kimberly.wma.adapter.AccountSearchAdapter
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.PopupAccountSearchBinding
import kr.co.kimberly.wma.model.AccountSearchModel


class PopupAccountSearch(mContext: Context): Dialog(mContext) {
    private lateinit var mBinding: PopupAccountSearchBinding

    private var context = mContext

    var onItemSelect: ((AccountSearchModel) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = PopupAccountSearchBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initViews()
    }

    private fun initViews() {
        // setCancelable(false) // 뒤로가기 버튼, 바깥 화면 터치시 닫히지 않게

        // (중요) Dialog 는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius 가 보이지 않음
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val list = ArrayList<AccountSearchModel>()
        for(i: Int in 1..5) {
            list.add(AccountSearchModel(""))
        }

        val adapter = AccountSearchAdapter(context)
        adapter.dataList = list
        mBinding.recyclerview.adapter = adapter
        mBinding.recyclerview.layoutManager = LinearLayoutManager(context)

        if(list.size > 10) {
            Utils.dialogResize(context, window)
        }

        mBinding.btLogin.setOnClickListener(object : OnSingleClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSingleClick(v: View) {
                if (mBinding.etAccount.text.isNullOrEmpty()) {
                    Toast.makeText(context, "거래처를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    list.clear()
                    for(i: Int in 1..5) {
                        list.add(AccountSearchModel("(000018) 신림마트 [${i}원]"))
                        adapter.notifyDataSetChanged()
                    }
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(mBinding.etAccount.windowToken, 0)
                }
            }
        })

        mBinding.etAccount.addTextChangedListener {
            if (mBinding.etAccount.text.isNullOrEmpty()) {
                mBinding.btProductNameEmpty.visibility = View.GONE
            } else {
                mBinding.btProductNameEmpty.visibility = View.VISIBLE
            }
        }

        mBinding.btProductNameEmpty.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                mBinding.etAccount.text = null
            }
        })

        adapter.itemClickListener = object: AccountSearchAdapter.ItemClickListener {
            override fun onItemClick(item: AccountSearchModel) {
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
    fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(mBinding.btLogin.windowToken, 0) //hide keyboard
            return true
        }
        return false
    }

}