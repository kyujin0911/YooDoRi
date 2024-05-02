package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemUpdateRateBinding

import kr.ac.tukorea.whereareu.domain.setting.UpdateRate

class UpdateRateRVA(
    private val list: ArrayList<UpdateRate>,
    private var selectedPosition: Int
) : ListAdapter<UpdateRate, UpdateRateRVA.TimeViewHolder>(diffUtil) {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(item: UpdateRate, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class TimeViewHolder(
        private val binding: ItemUpdateRateBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UpdateRate) {
            binding.model = item

            if (selectedPosition == bindingAdapterPosition) {
                list[bindingAdapterPosition].check = true
                binding.setChecked()
            } else {
                list[bindingAdapterPosition].check = false
                binding.setUnchecked()
            }
            if (onItemClickListener != null) {
                binding.root.setOnClickListener {
                    onItemClickListener?.onItemClick(item, bindingAdapterPosition)

                    if (selectedPosition != bindingAdapterPosition) {
                        binding.setChecked()
                        notifyItemChanged(selectedPosition)
                        selectedPosition = bindingAdapterPosition
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        return TimeViewHolder(
            ItemUpdateRateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    override fun getItemCount(): Int = list.size
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<UpdateRate>() {
            override fun areItemsTheSame(oldItem: UpdateRate, newItem: UpdateRate): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: UpdateRate, newItem: UpdateRate): Boolean {
                return oldItem == newItem
            }
        }
    }
    private fun ItemUpdateRateBinding.setChecked() {
        checkIv.visibility = View.VISIBLE
    }
    private fun ItemUpdateRateBinding.setUnchecked() {
        checkIv.visibility = View.GONE
    }
}