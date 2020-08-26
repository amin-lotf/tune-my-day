package com.aminook.tunemyday.framework.presentation.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Day
import kotlinx.android.synthetic.main.day_item.view.*

class DaysAdapter(private val days:List<Day>):RecyclerView.Adapter<DaysAdapter.ViewHolder>() {

    private var listener:OnDayClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.day_item,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day=days[position]
        holder.bind(day)
    }

    override fun getItemCount()=days.size

    fun setOnDayClickListener(listener: OnDayClickListener){
        this.listener=listener
    }

    private fun updateDays(chosenDay:Day){
        for ((index,day) in days.withIndex()){
            if (day!=chosenDay && day.isChosen){
                day.isChosen=false
                notifyItemChanged(index)
            }
            else if (day==chosenDay && !day.isChosen){
                day.isChosen=true
                notifyItemChanged(index)
            }

        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(day:Day){

            itemView.txt_day_long.text=day.fullName
            itemView.radio_day.isSelected=day.isChosen

            itemView.setOnClickListener {
                updateDays(day)
                listener?.onDayClick(day)
            }
        }

    }
}
interface OnDayClickListener{
    fun onDayClick(day:Day)
}