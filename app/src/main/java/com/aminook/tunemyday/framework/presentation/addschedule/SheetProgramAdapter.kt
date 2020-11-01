package com.aminook.tunemyday.framework.presentation.addschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Program
import kotlinx.android.synthetic.main.program_item.view.*

class SheetProgramAdapter():RecyclerView.Adapter<SheetProgramAdapter.ViewHolder>() {

    private var programs:List<Program> = listOf()

    private var listener:ProgramClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.program_item,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program=programs[position]
        holder.bind(program)
    }

    override fun getItemCount()=programs.size

    fun setProgramClickListener(listener: ProgramClickListener){
        this.listener=listener
    }

    fun submitList(programs:List<Program>){
        this.programs=programs
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {

        fun bind(program:Program){
            itemView.txt_program_choose_title.text=program.name

            itemView.card_choose_program.strokeColor = program.color
            itemView.layout_child__choose_program.setBackgroundColor(program.color)
            itemView.layout_child__choose_program.background.alpha=10

            itemView.setOnClickListener {

                listener?.addProgramClick(program)
            }
        }
    }
}

interface ProgramClickListener{
    fun addProgramClick(program: Program)
}