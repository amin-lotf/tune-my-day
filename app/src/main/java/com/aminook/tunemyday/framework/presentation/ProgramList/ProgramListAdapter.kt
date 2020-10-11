package com.aminook.tunemyday.framework.presentation.ProgramList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.util.ItemMoveCallback
import kotlinx.android.synthetic.main.program_detail_item.view.*

class ProgramListAdapter :
    ListAdapter<ProgramDetail, ProgramListAdapter.ViewHolder>(DIFF_CALLBACK), ItemMoveCallback {

    private var listener:ProgramDetailListener?=null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.program_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program=currentList[position]
        holder.bind(program)
    }

    fun setListener(programDetailListener: ProgramDetailListener){
        listener=programDetailListener
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(programDetail: ProgramDetail){
            itemView.txt_program_detail_title.text=programDetail.program.name
            itemView.txt_lower_label_program.setBackgroundColor(programDetail.program.color)
            itemView.card_program.strokeColor = programDetail.program.color
            itemView.layout_child_program.setBackgroundColor(programDetail.program.color)
            itemView.layout_child_program.background.alpha=10
            itemView.txt_num_schedules.text=programDetail.schedules.size.toString()
            itemView.txt_num_todos.text=programDetail.todos.size.toString()

            itemView.setOnClickListener {

                listener?.onProgramClick(programDetail)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProgramDetail>() {
            override fun areItemsTheSame(oldItem: ProgramDetail, newItem: ProgramDetail): Boolean {
                return oldItem.program.id == newItem.program.id
            }

            override fun areContentsTheSame(
                oldItem: ProgramDetail,
                newItem: ProgramDetail
            ): Boolean {
                Log.d("aminjoon", "areContentsTheSame: ${oldItem.program.id == newItem.program.id &&
                        oldItem.program.name == newItem.program.name &&
                        oldItem.schedules.size==newItem.schedules.size &&
                        oldItem.todos.size==newItem.todos.size &&
                        oldItem.program.color==newItem.program.color} ")


                return oldItem.program.id == newItem.program.id &&
                        oldItem.program.name == newItem.program.name &&
                        oldItem.schedules.size==newItem.schedules.size &&
                        oldItem.todos.size==newItem.todos.size &&
                        oldItem.program.color==newItem.program.color
            }
        }
    }


    override fun onItemSwap(fromPosition: Int, toPosition: Int) {
        // Do Nothing
    }

    override fun onItemSwipe(itemPosition: Int, direction: Int) {
        listener?.onProgramSwipe(currentList[itemPosition])
    }

    interface ProgramDetailListener{
        fun onProgramClick(program:ProgramDetail)
        fun onProgramSwipe(programDetail: ProgramDetail)
    }

}