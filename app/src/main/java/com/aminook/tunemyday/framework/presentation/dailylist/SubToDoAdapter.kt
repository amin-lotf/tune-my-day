package com.aminook.tunemyday.framework.presentation.dailylist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.SubToDo
import kotlinx.android.synthetic.main.todo_sub_item.view.*

class SubToDoAdapter : RecyclerView.Adapter<SubToDoAdapter.ViewHolder>() {
    private val subTodos = mutableListOf<SubToDo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_sub_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subTodo=subTodos[position]
        holder.bind(subTodo)
    }

    override fun getItemCount()=subTodos.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(subTodo:SubToDo){
            itemView.radio_sub_todo.text=subTodo.title
            if (subTodo.isDone){
                itemView.radio_sub_todo.apply {
                    isSelected=true
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }else{
                itemView.radio_sub_todo.apply {
                    isSelected=false
                    paintFlags = paintFlags and  Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

        }
    }
}