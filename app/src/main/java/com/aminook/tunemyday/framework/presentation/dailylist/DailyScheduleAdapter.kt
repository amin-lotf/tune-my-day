package com.aminook.tunemyday.framework.presentation.dailylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import kotlinx.android.synthetic.main.daily_schedule_item.view.*

class DailyScheduleAdapter: RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder>() {
    private var schedules= listOf<Schedule>()
    private var listener:DailyScheduleAdapterListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.daily_schedule_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule=schedules[position]
        holder.bind(schedule)
        listener?.setTodoAdapter(holder,schedule)
    }

    override fun getItemCount()=schedules.size

    fun setListener(listener: DailyScheduleAdapterListener){
        this.listener=listener
    }

    fun submitList(schedules:List<Schedule>){
        this.schedules=schedules
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoRecyclerView=itemView.recycler_schedule_todo
        fun bind(schedule: Schedule){
            itemView.txt_daily_start_time.text=schedule.startTime.toString()
            itemView.txt_daily_end_time.text=schedule.endTime.toString()
            itemView.txt_daily_program_name.text=schedule.program?.name
            itemView.img_add_todo.setOnClickListener {
              listener?.onAddNoteClick(schedule.id)
            }
            itemView.txt_daily_program_name.setOnClickListener {
                listener?.onScheduleClick(schedule.id)
            }
        }
    }

    interface DailyScheduleAdapterListener{
        fun setTodoAdapter(holder:ViewHolder,schedule: Schedule)
        fun onAddNoteClick(scheduleId:Long)
        fun onScheduleClick(scheduleId:Long)
    }
}