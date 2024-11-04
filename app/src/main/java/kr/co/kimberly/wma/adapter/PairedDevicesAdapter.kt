package kr.co.kimberly.wma.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.kimberly.wma.R
import kr.co.kimberly.wma.common.Define
import kr.co.kimberly.wma.common.SharedData
import kr.co.kimberly.wma.common.Utils
import kr.co.kimberly.wma.custom.OnSingleClickListener
import kr.co.kimberly.wma.databinding.CellPairedDevicesBinding
import kr.co.kimberly.wma.databinding.HeaderSettingBinding
import kr.co.kimberly.wma.network.model.DeviceModel


@SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
class PairedDevicesAdapter(context: Context, activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var dataList: List<Pair<String, String>> = ArrayList()
    var mContext = context
    var mActivity = activity
    var itemClickListener: ItemClickListener? = null
    var onItemSelect: ((DeviceModel) -> Unit)? = null // 기기 선택 시

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(HeaderSettingBinding.inflate(inflater, parent, false))
            else -> ViewHolder(CellPairedDevicesBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                holder.bind(dataList[position - 1])
            }
            is HeaderViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = dataList.size + 1

    inner class ViewHolder(val binding: CellPairedDevicesBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: Pair<String, String>) {

            val name = itemModel.first
            val address = itemModel.second
            binding.deviceName.text = name
            binding.deviceAddress.text = address

            itemView.setOnClickListener(object: OnSingleClickListener(){
                override fun onSingleClick(v: View) {
                    onItemSelect?.invoke(DeviceModel(name, address))
                }
            })

            itemClickListener = object : ItemClickListener{
                override fun connected() {
                    binding.connected.visibility = View.VISIBLE
                    binding.deviceBox.background = mContext.getDrawable(R.drawable.ll_round_97bcf3)
                    if (name.startsWith(Define.SCANNER_NAME)){
                        SharedData.setSharedDataModel(mContext, "scanner", DeviceModel(name, address))
                    } else {
                        SharedData.setSharedDataModel(mContext, "printer", DeviceModel(name, address))
                    }
                }
                override fun disconnected() {
                    binding.connected.visibility = View.GONE
                    binding.deviceBox.background = mContext.getDrawable(R.drawable.ll_round)
                    if (name.startsWith(Define.SCANNER_NAME)){
                        SharedData.setSharedDataModel(mContext, "scanner", null)
                    } else {
                        SharedData.setSharedDataModel(mContext, "printer", null)
                    }
                }
            }
            val printer = SharedData.getSharedDataModel(mContext, "printer", DeviceModel::class.java)
            val scanner = SharedData.getSharedDataModel(mContext, "scanner", DeviceModel::class.java)

            if (printer != null || scanner != null) {
                binding.connected.visibility = View.VISIBLE
                binding.deviceBox.background = mContext.getDrawable(R.drawable.ll_round_97bcf3)
            }
        }
    }

    inner class HeaderViewHolder(val binding: HeaderSettingBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind() {
            // 데이터가 없으면 divide line 안보이도록
            if (dataList.isNotEmpty()){
                binding.bottomDivideLine.visibility = View.VISIBLE
            } else {
                binding.bottomDivideLine.visibility = View.GONE
            }

            val printer = SharedData.getSharedDataModel(mContext, "printer", DeviceModel::class.java)
            val scanner = SharedData.getSharedDataModel(mContext, "scanner", DeviceModel::class.java)

            if (printer != null) {
                binding.printName.text = printer.deviceName
                binding.printAddress.text = printer.deviceAddress
                binding.printName.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                binding.printAddress.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                itemClickListener?.connected()
            }

            if (scanner != null) {
                binding.scanerName.text = scanner.deviceName
                binding.scanerAddress.text = scanner.deviceAddress
                binding.scanerName.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                binding.scanerAddress.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                itemClickListener?.connected()
            }

            onItemSelect = {
                if (it.deviceName.startsWith(Define.SCANNER_NAME)){
                    if (binding.scanerAddress.text == it.deviceAddress){
                        binding.scanerName.text = null
                        binding.scanerAddress.text = null
                        binding.scanerName.hint = mContext.getString(R.string.settingTitleHint02)
                        binding.scanerAddress.hint = mContext.getString(R.string.settingTitleHint02)
                        binding.scanerName.background = mContext.getDrawable(R.drawable.et_round_c9cbd0)
                        binding.scanerAddress.background = mContext.getDrawable(R.drawable.et_round_c9cbd0)
                        itemClickListener?.disconnected()
                    } else {
                        binding.scanerName.text = it.deviceName
                        binding.scanerAddress.text = it.deviceAddress
                        binding.scanerName.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                        binding.scanerAddress.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                        itemClickListener?.connected()
                    }
                } else {
                    if (binding.printAddress.text == it.deviceAddress){
                        binding.printName.text = null
                        binding.printAddress.text = null
                        binding.printName.hint = mContext.getString(R.string.settingTitleHint02)
                        binding.printAddress.hint = mContext.getString(R.string.settingTitleHint02)
                        binding.printName.background = mContext.getDrawable(R.drawable.et_round_c9cbd0)
                        binding.printAddress.background = mContext.getDrawable(R.drawable.et_round_c9cbd0)
                        itemClickListener?.disconnected()
                    } else {
                        binding.printName.text = it.deviceName
                        binding.printAddress.text = it.deviceAddress
                        binding.printName.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                        binding.printAddress.background = mContext.getDrawable(R.drawable.ll_round_c9cbd0)
                        itemClickListener?.connected()
                    }
                }
            }
        }
    }
    interface ItemClickListener {
        fun connected()
        fun disconnected()
    }
}