package com.aminook.tunemyday.framework.presentation.weeklylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Day
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.android.synthetic.main.weekly_item_view_pager.view.*
import javax.inject.Inject


class WeekViewPagerAdapter():RecyclerView.Adapter<WeekViewPagerAdapter.ViewHolder>() {

    private var days:List<Day> = emptyList()

    private var listener:WeeklyRecyclerViewListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.weekly_item_view_pager,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

       // listener?.setAdapter(holder,position)
    }



    override fun getItemCount()=days.size



    fun setListener(listener:WeeklyRecyclerViewListener){
        this.listener=listener
    }

    fun submitList(days:List<Day>){
        this.days=days
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var recyclerView: RecyclerView = itemView.daily_schedules_recycler
    }

    interface WeeklyRecyclerViewListener{
        fun setAdapter(itemView: ViewHolder,position: Int)

    }
}


