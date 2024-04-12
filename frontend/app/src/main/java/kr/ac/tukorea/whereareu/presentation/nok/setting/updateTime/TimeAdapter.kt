package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.SettingUpdateTimeOptionItemBinding
import kr.ac.tukorea.whereareu.presentation.nok.setting.NokSettingFragment
import kotlin.text.Typography.times

class TimeAdapter(
    private val list: ArrayList<TimeData>,
    private var selectedPosition : Int
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    lateinit var binding: SettingUpdateTimeOptionItemBinding
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(item: TimeData, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class TimeViewHolder(
        private val binding: SettingUpdateTimeOptionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TimeData) {
            binding.model = item

            if (selectedPosition == absoluteAdapterPosition) {
                list[absoluteAdapterPosition].check = true
                binding.setChecked()
            } else {
                list[absoluteAdapterPosition].check = false
                binding.setUnchecked()

            }
            if (onItemClickListener != null) {
                binding.root.setOnClickListener{
                    onItemClickListener?.onItemClick(item, absoluteAdapterPosition)

                    if (selectedPosition != absoluteAdapterPosition) {
                        binding.setChecked()
                        Log.d("Adapter_checked", "$absoluteAdapterPosition, $selectedPosition")
                        notifyItemChanged(selectedPosition)
                        selectedPosition = absoluteAdapterPosition
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        binding = SettingUpdateTimeOptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeViewHolder(binding)
    }
    override fun onBindViewHolder(holder: TimeViewHolder, position: Int){
        holder.bind(list[position])
        // viewHolder 개수만큼 호출
    }

    override fun getItemCount(): Int = list.size

    private fun SettingUpdateTimeOptionItemBinding.setChecked() {
        checkIv.visibility = View.VISIBLE
    }

    private fun SettingUpdateTimeOptionItemBinding.setUnchecked() {
        checkIv.visibility = View.GONE
    }
}
