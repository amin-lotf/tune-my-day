package com.aminook.tunemyday.framework.presentation.addschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Alarm
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_ADDED
import com.aminook.tunemyday.framework.presentation.addschedule.manager.AddScheduleManager.Companion.ALARM_REMOVED
import kotlinx.android.synthetic.main.alarm_item.view.*

class AlarmListAdapter() : RecyclerView.Adapter<AlarmListAdapter.ViewHolder>() {
    private val TAG="aminjoon"
    private var alarms: List<Alarm> = emptyList()


    private var listener: AlarmClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bind(alarm)
    }

    override fun getItemCount(): Int = alarms.size

    fun notifyListChanged(type: String, position: Int) {

        when (type) {
            ALARM_ADDED -> notifyItemInserted(position)
            ALARM_REMOVED -> notifyItemRemoved(position)
        }
    }

    fun setOnClickListener(listener: AlarmClickListener) {
        this.listener = listener
    }

    fun submitList(alarms: List<Alarm>) {
        this.alarms = alarms
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(alarm: Alarm) {
            itemView.add_schedule_notification.text = alarm.toString()
            itemView.add_schedule_remove_alarm.setOnClickListener {
                listener?.onRemoveClick(alarm)
            }
            itemView.setOnClickListener {
                listener?.onAlarmClick(alarm)
            }
        }
    }

    interface AlarmClickListener {
        fun onRemoveClick(alarm: Alarm)
        fun onAlarmClick(alarm: Alarm)
    }
}