package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Color
import kotlinx.android.synthetic.main.color_item.view.*

class ProgramColorsAdapter(private val colors:List<Color>):RecyclerView.Adapter<ProgramColorsAdapter.ViewHolder>() {
    private val TAG="aminjoon"


    private var _selectedColor:Color?= if (colors.size>0) colors[0] else null

    val selectedColor:Color?
    get() = _selectedColor

    var listener:OnColorClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.color_item,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color=colors[position]
        holder.bind(color)
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    fun setOnColorClickListener(listener: OnColorClickListener){
        this.listener=listener
    }

    private fun updateColors(chosenColor: Color){
        for((index,color) in colors.withIndex()){
            if(color!= chosenColor && color.isChosen){
                color.isChosen=false
                notifyItemChanged(index)
            }
            else if (color==chosenColor && !color.isChosen){
                color.isChosen=true
                notifyItemChanged(index)
            }
        }
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(color: Color){
            itemView.img_color.setBackgroundColor(color.value)

            if(color.isChosen){
                itemView.img_chosen.visibility=View.VISIBLE
            }else{
                itemView.img_chosen.visibility=View.GONE
            }

            itemView.setOnClickListener {
                updateColors(color)
                _selectedColor=color
                listener?.onSelectColor(color)
            }


        }
    }
}

interface OnColorClickListener{
    fun onSelectColor(color: Color)
}