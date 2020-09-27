package com.aminook.tunemyday.framework.presentation.addschedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo

class ToDoListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG="aminjoon"
    private val TYPE_ITEM = 1
    private val TYPE_ADD = 2

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (position == differ.currentList.size) {
            return TYPE_ADD
        } else {
            return TYPE_ITEM
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==TYPE_ITEM) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
            return ToDoViewHolder(view)
        }else{
            val view=LayoutInflater.from(parent.context).inflate(R.layout.todo_add_item,parent,false)
            return  AddToDoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_ITEM) {
            val item = differ.currentList[position]
            (holder as ToDoViewHolder).bind(item)
        }
    }

    fun submitList(todos:List<Todo>){
        differ.submitList(todos)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size+1
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: Todo) {

        }
    }



    inner class  AddToDoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }
}