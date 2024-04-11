package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.NonDisposableHandle.parent
import kr.ac.tukorea.whereareu.databinding.SettingUpdateTimeOptionItemBinding
import kr.ac.tukorea.whereareu.presentation.nok.setting.NokSettingFragment

class TimeAdapter(
    private val times: ArrayList<TimeData>,
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder{
//        val view = SettingUpdateTimeOptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeViewHolder(
            SettingUpdateTimeOptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                // LayoutInflater.from을 통해 LayoutInflater을 받을 수 있음
                // parent가 ViewGroup이고, ViewGroup는 View를 상속하므로 이미 View를 가지고 있음
                parent,
                // parent는 RecyclerView 자체임
                false
                // parent를 연결 시켜 주는 것, 이 부분은 RecyclerView가 알아서 해야함
            )
        )
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(times[position])
        holder.itemView.visibility
        holder.itemView.visibility
    }

    override fun getItemCount(): Int = times.size
    interface OnItemClickListener {
        fun onItemClick(timeOption: String)
    }

    class TimeViewHolder(private val binding: SettingUpdateTimeOptionItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(time: TimeData){
            binding.updateTimeTv.text = time.title
        }
    }


//    ---------------------------------------------

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ItemTimeBinding.inflate(inflater, parent, false)
//        return TimeViewHolder(bin을ding)
//    }
//
//    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
//        val timeOption = timeOptions[position]
//        holder.bind(timeOption)
//    }
//
//    override fun getItemCount(): Int = timeOptions.size
//
//    inner class TimeViewHolder(private val binding: ItemTimeBinding) :
//        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
//
//        init {
//            binding.root.setOnClickListener(this)
//        }
//
//        fun bind(timeOption: String) {
//            binding.timeTextView.text = timeOption
//        }
//
//        override fun onClick(v: View?) {
//            val position = adapterPosition
//            if (position != RecyclerView.NO_POSITION) {
//                val selectedTime = timeOptions[position]
//                itemClickListener.onItemClick(selectedTime)
//            }
//        }
//    }
}
