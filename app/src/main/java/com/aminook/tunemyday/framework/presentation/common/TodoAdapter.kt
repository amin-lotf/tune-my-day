package com.aminook.tunemyday.framework.presentation.common

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
import com.aminook.tunemyday.util.ItemMoveCallback
import kotlinx.android.synthetic.main.todo_item.view.*

class TodoAdapter : ListAdapter<Todo, TodoAdapter.ViewHolder>(DIFF_CALLBACK), ItemMoveCallback {

    private val TAG = "aminjoon"
    private var listener: ToDoRecyclerViewListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = getItem(position)
        holder.bind(todo)
    }

    override fun submitList(list: List<Todo>?) {
        if (list.isNullOrEmpty()){
            listener?.onEmptyList()
        }else{
            listener?.onNonEmptyList()
        }
        super.submitList(list)
    }

    fun setListener(listener: ToDoRecyclerViewListener) {
        this.listener = listener
    }


    override fun onItemSwap(fromPosition: Int, toPosition: Int) {
        listener?.swapItems(fromPosition, toPosition)
    }

    override fun onItemSwipe(itemPosition: Int, direction: Int) {

    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(todo: Todo) {

            itemView.txt_todo_title.text = todo.title
            if (todo.isDone) {
                itemView.chk_todo.apply {
                    isChecked = true
                    Log.d(TAG, "bind: is checked")
                    itemView.txt_todo_title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
            } else {
                itemView.chk_todo.apply {
                    isChecked = false
                    itemView.txt_todo_title.paintFlags = Paint.ANTI_ALIAS_FLAG
                }
            }

            itemView.chk_todo.setOnClickListener {
                listener?.onCheckChanged(
                    todo.copy(isDone = itemView.chk_todo.isChecked)
                )
            }

            itemView.img_remove_todo.setOnClickListener {
                Log.d(TAG, "bind: delete todo adapter ")
                listener?.onDeleteTodoClick(todo)
            }
            itemView.txt_todo_title.setOnClickListener {
                listener?.onEditTodoClick(todo)
            }

        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                Log.d("aminjoon", "areContentsTheSame: ${oldItem.isDone == newItem.isDone}")
                return oldItem.title == newItem.title &&
                        oldItem.dateAdded == newItem.dateAdded &&
                        oldItem.isDone == newItem.isDone &&
                        oldItem.priorityIndex == newItem.priorityIndex
            }

        }
    }

    interface ToDoRecyclerViewListener {
        fun onDeleteTodoClick(todo: Todo)
        fun onEditTodoClick(todo: Todo)
        fun onCheckChanged(todo: Todo)
        fun swapItems(fromPosition: Int, toPosition: Int)
        fun onEmptyList()
        fun onNonEmptyList()
    }


}