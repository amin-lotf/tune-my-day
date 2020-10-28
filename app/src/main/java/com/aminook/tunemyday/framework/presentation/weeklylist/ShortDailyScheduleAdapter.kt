package com.aminook.tunemyday.framework.presentation.weeklylist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.presentation.common.BaseViewHolder
import kotlinx.android.synthetic.main.no_schedule_item.view.*
import kotlinx.android.synthetic.main.schedule_item.view.*

class ShortDailyScheduleAdapter(val context: Context,val curDay:Int) :
    RecyclerView.Adapter<BaseViewHolder<Schedule>>() {

    private var listener: ItemClickListener? = null
    private val TYPE_BUSY = 1
    private val TYPE_FREE = 2
    private val TYPE_LAST=3

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.hasAlarm == newItem.hasAlarm &&
                    oldItem.hasToDo == newItem.hasToDo &&
                    oldItem.startTime == newItem.startTime &&
                    oldItem.endTime == newItem.endTime &&
                    oldItem.program.name == newItem.program.name &&
                    oldItem.program.color == newItem.program.color
        }

    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Schedule> {

        if (viewType == TYPE_BUSY) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
            return BusyViewHolder(view)
        } else if (viewType==TYPE_FREE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.no_schedule_item, parent, false)
            return FreeViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.last_item_empty, parent, false)
            return LASTViewHolder(view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        val item = differ.currentList[position]
        return if (item.id == -1L) {
            TYPE_FREE
        } else if (item.id==-2L){
            TYPE_LAST
        }else{
            TYPE_BUSY
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Schedule>, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun submitList(schedules: List<Schedule>) {
        differ.submitList(schedules)
        //TODO(Check if notify db is needed)
    }

    inner class BusyViewHolder(itemView: View) : BaseViewHolder<Schedule>(itemView) {

        override fun bind(item: Schedule) {
            itemView.schedule_program.text = item.program.name
            val label = ContextCompat.getDrawable(context, R.drawable.program_label)
            label?.setTint(item.program.color)
            //  itemView.program_color.background=label
            //  itemView.program_color.
            itemView.layout_schedule_item.setStrokeColor(item.program.color)
            itemView.layout_child_schedule_item.setBackgroundColor(item.program.color)
            itemView.layout_child_schedule_item.background.alpha = 10
            itemView.schedule_hour_start.text = item.startTime.toString()
            itemView.schedule_hour_end.text = item.endTimeFormatted.toString()
            itemView.schedule_duration.text = item.duration
            if (item.hasToDo) {
                itemView.schedule_todo__on.visibility = View.VISIBLE
                itemView.schedule_todo__off.visibility = View.INVISIBLE
            } else {
                itemView.schedule_todo__on.visibility = View.INVISIBLE
                itemView.schedule_todo__off.visibility = View.VISIBLE
            }

            if (item.hasAlarm) {
                itemView.schedule_alarm_icon_on.visibility = View.VISIBLE
                itemView.schedule_alarm_icon_off.visibility = View.INVISIBLE
            } else {
                itemView.schedule_alarm_icon_on.visibility = View.INVISIBLE
                itemView.schedule_alarm_icon_off.visibility = View.VISIBLE
            }

            if (item.startDay==curDay && item.startDay != item.endDay && (item.endTime.hour!=0 || item.endTime.minute!=0)) {
                itemView.txt_next_day.visibility = View.VISIBLE
            }

            if (item.startDay!=curDay){
                itemView.txt_prev_day.visibility=View.VISIBLE
            }

            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }

        }

    }

    inner class FreeViewHolder(itemView: View) :BaseViewHolder<Schedule>(itemView) {
        override fun bind(item: Schedule) {
            itemView.txt_zzz_start.text=item.startTime.toString()
            itemView.txt_zzz_end.text=item.endTime.toString()
            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
    }

    inner class LASTViewHolder(itemView: View):BaseViewHolder<Schedule>(itemView){
        override fun bind(item: Schedule) {

        }
    }


}

interface ItemClickListener {
    fun onItemClick(schedule: Schedule)
}