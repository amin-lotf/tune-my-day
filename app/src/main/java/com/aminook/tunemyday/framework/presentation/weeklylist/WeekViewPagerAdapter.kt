package com.aminook.tunemyday.framework.presentation.weeklylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import kotlinx.android.synthetic.main.weekly_item_view_pager.view.*

class WeekViewPagerAdapter(val names:List<String>):RecyclerView.Adapter<WeekViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view=LayoutInflater.from(parent.context).inflate(R.layout.weekly_item_view_pager,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text=names[position]
       // holder.itemView.txt_test.text=text

    }

    override fun getItemCount()=names.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}