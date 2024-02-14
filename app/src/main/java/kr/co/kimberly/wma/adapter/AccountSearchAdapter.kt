package kr.co.kimberly.wma.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.databinding.CellAccountSearchBinding
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.model.AccountSearchModel
import kr.co.kimberly.wma.model.MainMenuModel
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AccountSearchAdapter(context: Context): RecyclerView.Adapter<AccountSearchAdapter.ViewHolder>() {
    var dataList: List<AccountSearchModel> = ArrayList()
    var itemClickListener: ItemClickListener? = null
    var mContext = context

    inner class ViewHolder(val binding: CellAccountSearchBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: AccountSearchModel) {
            binding.accountSearchName.text = itemModel.name

            itemView.setOnClickListener {
                itemClickListener?.onItemClick(itemModel)
                /*val intent = Intent(itemView.context, MessageActivity::class.java)
                intent.putExtra(Define.UNIQUE, itemModel.name)
                intent.putExtra(Define.D_COUNT, binding.dCount.text.toString())
                intent.putExtra(Define.MEMBER_TYPE, itemModel.type)
                intent.putExtra(Define.MAIN_COLOR, itemModel.color)
                intent.putExtra(Define.MAIN_NAME, itemModel.name_kor)
                intent.putExtra(Define.TOP_THUMB, itemModel.top_thumbnail)
                intent.putExtra(Define.TOP_THUMB_LINK, itemModel.top_thumbnail_link)
                intent.putExtra(Define.CHANGE_THUMB, itemModel.change_thumb)
                intent.putExtra(Define.MENU_SW, isMenuSw)
                itemView.context.startActivity(intent)
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)*/
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CellAccountSearchBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])

        if(position == (itemCount - 1)) {
            holder.binding.line.visibility = View.INVISIBLE
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: AccountSearchModel)
    }
}