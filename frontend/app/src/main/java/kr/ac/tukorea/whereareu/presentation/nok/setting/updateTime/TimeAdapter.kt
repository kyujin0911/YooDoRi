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

    //RecyclerView의 각 항목에 대한 바인딩을 위한 변수를 선언
    private var onItemClickListener: OnItemClickListener? = null

    // null 예외처리

    interface OnItemClickListener {
        // RecyclerView의 항목을 클릭할 때 호출될 콜백을 정의하는 인터페이스 선언
        fun onItemClick(item: TimeData, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        // 항목 클릭 리스너를 설정하는 메서드 정의
        this.onItemClickListener = listener
    }

    inner class TimeViewHolder(
        private val binding: SettingUpdateTimeOptionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        // RecyclerView의 각 항목을 보유할 뷰홀더 클래스를 정의
        fun bind(item: TimeData) {
            binding.model = item

            Log.d("absoultePosition", "$absoluteAdapterPosition")
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
                    Log.d("Adapter", "$absoluteAdapterPosition, $selectedPosition")

                    if (selectedPosition != absoluteAdapterPosition) {
                        binding.setChecked()
                        Log.d("Adapter_checked", "$absoluteAdapterPosition, $selectedPosition")
                        notifyItemChanged(selectedPosition)
                        // slelctedPosition에 대한 아이템만 view를 새로 그림
//                        notifyDataSetChanged()
                        // 모든 view를 전부 다 새로 그림
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
