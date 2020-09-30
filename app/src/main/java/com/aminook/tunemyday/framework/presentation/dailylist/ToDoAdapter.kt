package com.aminook.tunemyday.framework.presentation.dailylist

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import kotlinx.android.synthetic.main.todo_item.view.*

class ToDoAdapter: ListAdapter<Todo,ToDoAdapter.ViewHolder>(DIFF_CALLBACK){

    private val TAG="aminjoon"
    private var listener:ToDoRecyclerViewListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo=getItem(position)
        holder.bind(todo)
        listener?.setSubTodoAdapter(holder,todo)
    }


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

            itemView.img_remove_todo.setOnClickListener {
                Log.d(TAG, "bind: delete todo adapter ")
                listener?.onDeleteTodoClick(todo)
            }

        }
    }

    companion object{
        private val DIFF_CALLBACK= object :DiffUtil.ItemCallback<Todo>(){
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
               return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.dateAdded==newItem.dateAdded &&
                        oldItem.isDone==newItem.isDone &&
                        oldItem.priorityIndex==newItem.priorityIndex
            }

        }
    }

    interface ToDoRecyclerViewListener{
        fun setSubTodoAdapter(itemView: ToDoAdapter.ViewHolder, todo:Todo)
        fun onDeleteTodoClick(todo:Todo)
    }
}