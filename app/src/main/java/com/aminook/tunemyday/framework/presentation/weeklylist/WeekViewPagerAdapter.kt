package com.aminook.tunemyday.framework.presentation.weeklylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R

class WeekViewPagerAdapter(val names:List<String>):RecyclerView.Adapter<WeekViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.fragment_weekly_list,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount()=names.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}