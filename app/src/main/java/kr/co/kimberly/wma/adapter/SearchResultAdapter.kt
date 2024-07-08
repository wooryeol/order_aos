package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.databinding.CellSearchResultBinding
import kr.co.kimberly.wma.network.model.CustomerModel
import kr.co.kimberly.wma.network.model.SearchItemModel

class SearchResultAdapter(context: Context): RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {
    var dataList: List<SearchItemModel> = ArrayList()
    var itemClickListener: ItemClickListener? = null
    var mContext = context

    inner class ViewHolder(val binding: CellSearchResultBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: SearchItemModel) {
            if (itemModel.slipNo.isNullOrEmpty()){
                binding.accountSearchName.text = "(${itemModel.itemCd}) ${itemModel.itemNm} [${itemModel.whStock}]"
            } else {

            }

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
        val binding = CellSearchResultBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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
        fun onItemClick(item: SearchItemModel)
    }
}