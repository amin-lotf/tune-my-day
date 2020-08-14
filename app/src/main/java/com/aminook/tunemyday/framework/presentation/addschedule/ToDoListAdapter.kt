package com.aminook.tunemyday.framework.presentation.addschedule

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.ToDo

class ToDoListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG="aminjoon"
    private val TYPE_ITEM = 1
    private val TYPE_ADD = 2

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
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
        Log.d(TAG, "onCreateViewHolder: $viewType ")
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

    fun submitList(todos:List<ToDo>){
        differ.submitList(todos)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size+1
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun bind(item: ToDo) {

        }
    }



    inner class  AddToDoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }
}