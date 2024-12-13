package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.databinding.CellSearchResultBinding
import kr.co.kimberly.wma.network.model.SearchItemModel

class SearchResultAdapter(context: Context): RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {
    var dataList: List<SearchItemModel> = ArrayList()
    var itemClickListener: ItemClickListener? = null
    var mContext = context

    inner class ViewHolder(val binding: CellSearchResultBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemModel: SearchItemModel) {
            binding.accountSearchName.text = "(${itemModel.itemCd}) ${itemModel.itemNm} [${itemModel.whStock}]"

            itemView.setOnClickListener {
                Utils.log("clicked item ====> ${Gson().toJson(itemModel)}")
                // 본사 발주 가능일 때
                /*if(itemModel.enableOrderYn == "N") {
                    itemClickListener?.onItemClick(itemModel)
                } else {
                    //본사 발주 불가능일 때
                    val item = SpannableString("(${itemModel.itemCd}) ${itemModel.itemNm}")
                    item.setSpan(UnderlineSpan(), 0, item.length, 0)
                    PopupNotice(mContext, "(${itemModel.itemCd}) ${itemModel.itemNm}\n해당 제품은 현재 본사 발주가 불가능합니다.").show()
                }*/

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