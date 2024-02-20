package kr.co.kimberly.wma.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.databinding.CellMeinMenuBinding
import kr.co.kimberly.wma.menu.inventory.InventoryActivity
import kr.co.kimberly.wma.menu.collect.CollectManageActivity
import kr.co.kimberly.wma.menu.information.InformationActivity
import kr.co.kimberly.wma.menu.ledger.LedgerActivity
import kr.co.kimberly.wma.menu.order.OrderRegActivity
import kr.co.kimberly.wma.menu.purchase.PurchaseRequestActivity
import kr.co.kimberly.wma.menu.`return`.ReturnRegActivity
import kr.co.kimberly.wma.menu.slip.SlipInquiryActivity
import kr.co.kimberly.wma.model.MainMenuModel
import java.util.ArrayList

class MainMenuAdapter(context: Context, activity: Activity): RecyclerView.Adapter<MainMenuAdapter.ViewHolder>() {
    var dataList: List<MainMenuModel> = ArrayList()
    var mContext = context
    var mActivity = activity

    inner class ViewHolder(private val binding: CellMeinMenuBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: MainMenuModel) {
            binding.icon.setImageDrawable(ContextCompat.getDrawable(mContext, itemModel.image))
            binding.menuName.text = itemModel.menuName

            itemView.setOnClickListener {
                val intent: Intent? =  when(itemModel.type) {
                    Define.MENU01 -> { // 주문등록
                        Intent(itemView.context, OrderRegActivity::class.java)
                    }
                    Define.MENU02 -> { // 수금관리
                        Intent(itemView.context, CollectManageActivity::class.java)
                    }
                    Define.MENU03 -> { // 반품조회
                        Intent(itemView.context, ReturnRegActivity::class.java)
                    }
                    Define.MENU04 -> { // 전표조회
                        Intent(itemView.context, SlipInquiryActivity::class.java)
                    }
                    Define.MENU04 -> { // 전표조회
                        Intent(itemView.context, LedgerActivity::class.java)
                    }
                    Define.MENU06 -> { // 재고조회
                        Intent(itemView.context, InventoryActivity::class.java)
                    }
                    Define.MENU08 -> { // 구매요청
                        Intent(itemView.context, PurchaseRequestActivity::class.java)
                    }
                    Define.MENU09 -> { // 기준정보
                        Intent(itemView.context, InformationActivity::class.java)
                    }
                    else -> null
                }
                intent?.let {
                    itemView.context.startActivity(it)
                }
                /*if(itemModel.type == Define.MENU01) {
                    val intent = Intent(itemView.context, OrderRegActivity::class.java)
                    itemView.context.startActivity(intent)
                }

                if (itemModel.type == Define.MENU02) {
                    val intent = Intent(itemView.context, CollectActivity::class.java)
                    itemView.context.startActivity(intent)
                }*/
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
        val binding = CellMeinMenuBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }
}