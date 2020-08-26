package com.aminook.tunemyday.framework.presentation.weeklylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.ScheduleEntity
import kotlinx.android.synthetic.main.schedule_item.view.*
import java.util.zip.Inflater

class ShortDailyScheduleRecycler : RecyclerView.Adapter<ShortDailyScheduleRecycler.ViewHolder>() {

    private var listener:ItemClickListener?=null

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=differ.currentList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnClickListener(listener: ItemClickListener){
        this.listener=listener
    }

    fun submitList(schedules:List<Schedule>){
        differ.submitList(schedules)
        //TODO(Check if notify db is needed)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(schedule: Schedule) {
            itemView.schedule_program.text=schedule.program?.name
            itemView.schedule_hour_start.text=schedule.startTime.toString()
            itemView.schedule_hour_end.text=schedule.endTime.toString()
            itemView.schedule_duration.text=schedule.duration
            if (schedule.hasToDo){
                itemView.schedule_todo_icon.visibility=View.VISIBLE
            }else{
             itemView.schedule_todo_icon.visibility=View.GONE
            }

            if (schedule.hasAlarm){
                itemView.schedule_alarm_icon.visibility=View.VISIBLE
            }else{
                itemView.schedule_alarm_icon.visibility=View.GONE
            }

            itemView.setOnClickListener{
                listener?.onItemClick(schedule)
            }
        }

    }
}

interface ItemClickListener{
    fun onItemClick(schedule: Schedule)
}