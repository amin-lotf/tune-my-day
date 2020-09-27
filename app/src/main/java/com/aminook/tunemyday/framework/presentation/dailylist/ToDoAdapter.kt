package com.aminook.tunemyday.framework.presentation.dailylist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import kotlinx.android.synthetic.main.todo_item.view.*

class ToDoAdapter: RecyclerView.Adapter<ToDoAdapter.ViewHolder>(){

    private val todos = mutableListOf<Todo>()
    private var listener:ToDoRecyclerViewListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo=todos[position]
        holder.bind(todo)
        listener?.setSubTodoAdapter(holder,todo)
    }

    override fun getItemCount()=todos.size

    fun setListener(listener: ToDoRecyclerViewListener){
        this.listener=listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subTodoRecycler:RecyclerView=itemView.recycler_sub_todo
        fun bind(todo: Todo){
            itemView.radio_todo.text=todo.title
            if (todo.isDone){
                itemView.radio_todo.apply {
                    isSelected=true
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }else{
                itemView.radio_todo.apply {
                    isSelected=false
                    paintFlags = paintFlags and  Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

        }
    }

    interface ToDoRecyclerViewListener{
        fun setSubTodoAdapter(itemView: ToDoAdapter.ViewHolder, todo:Todo)

    }
}