package com.aminook.tunemyday.framework.presentation.addschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Program
import kotlinx.android.synthetic.main.bottom_sheet_programs.view.*
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
    }

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {

        fun bind(program:Program){
            itemView.txt_program_name.text=program.name
            itemView.img_program_label.setBackgroundColor(program.color)
            itemView.setOnClickListener {
                listener?.AddProgramClick(program)

            }
        }
    }
}

interface ProgramClickListener{
    fun AddProgramClick(program: Program)
}