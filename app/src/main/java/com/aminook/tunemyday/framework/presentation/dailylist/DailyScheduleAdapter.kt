package com.aminook.tunemyday.framework.presentation.dailylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.presentation.common.ToDoAdapter
import kotlinx.android.synthetic.main.daily_schedule_item.view.*

class DailyScheduleAdapter : ListAdapter<Schedule,DailyScheduleAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var listener: DailyScheduleAdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.daily_schedule_item, parent, false)
        return ViewHolder(view,this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
        listener?.setTodoAdapter(holder,schedule)


    }



    fun setListener(listener: DailyScheduleAdapterListener) {
        this.listener = listener
    }


    inner class ViewHolder(itemView: View, val dailyScheduleAdapter: DailyScheduleAdapter) : RecyclerView.ViewHolder(itemView) {

        val todoRecyclerView = itemView.recycler_schedule_todo
        fun bind(schedule: Schedule) {
            itemView.txt_daily_start_time.text = schedule.startTime.toString()
            itemView.txt_daily_end_time.text = schedule.endTime.toString()
            itemView.txt_daily_program_name.text = schedule.program?.name
            itemView.img_add_todo.setOnClickListener {
                listener?.onAddNoteClick(schedule.id,todoRecyclerView.adapter as ToDoAdapter)
            }
            itemView.txt_daily_program_name.setOnClickListener {
                listener?.onScheduleClick(schedule.id)
            }
        }



    }
    companion object{
        private val DIFF_CALLBACK= object : DiffUtil.ItemCallback<Schedule>(){
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id==newItem.id
            }


        }
    }


    interface DailyScheduleAdapterListener {
        fun setTodoAdapter(holder: ViewHolder, schedule: Schedule)
        fun onAddNoteClick(scheduleId: Long, dailyScheduleAdapter: ToDoAdapter)
        fun onScheduleClick(scheduleId: Long)
    }
}