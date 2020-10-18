package com.aminook.tunemyday.framework.presentation.common

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.util.ItemMoveCallback
import kotlinx.android.synthetic.main.todo_item.view.*

class TodoAdapter(val isSummary:Boolean=false) : ListAdapter<Todo, BaseViewHolder<Todo>>(DIFF_CALLBACK), ItemMoveCallback {

    private val TAG = "aminjoon"
    private var listener: ToDoRecyclerViewListener? = null

    private val TYPE_TODO=1
    private val TYPE_LAST=2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Todo> {
       if (viewType==TYPE_TODO){
           val view =
               LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
           return ViewHolder(view)
       }else{
           val view =
               LayoutInflater.from(parent.context)
                   .inflate(R.layout.last_todo_empty, parent, false)
           return LastItemViewHolder(view)
       }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Todo>, position: Int) {
        val todo = getItem(position)
        holder.bind(todo)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).id!= -1L){
            TYPE_TODO
        }else{
            TYPE_LAST
        }
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
        listener?.swipeToDelete(getItem(itemPosition))
    }

    inner class ViewHolder(itemView: View) :
        BaseViewHolder<Todo>(itemView) {
        override fun bind(item: Todo) {

            itemView.txt_todo_title.text = item.title
            if (!isSummary) {
                if (item.isDone) {
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
                        item.copy(isDone = itemView.chk_todo.isChecked)
                    )
                }
            }else
            {
                itemView.chk_todo.isEnabled=false
            }


            itemView.txt_todo_title.setOnClickListener {
                listener?.onEditTodoClick(item)
            }

        }
    }

    inner class LastItemViewHolder(itemView: View) : BaseViewHolder<Todo>(itemView) {
        override fun bind(item: Todo) {

        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.title == newItem.title &&
                        oldItem.dateAdded == newItem.dateAdded &&
                        oldItem.isDone == newItem.isDone &&
                        oldItem.priorityIndex == newItem.priorityIndex
            }

        }
    }

    interface ToDoRecyclerViewListener {
        fun onEditTodoClick(todo: Todo)
        fun onCheckChanged(todo: Todo)
        fun swapItems(fromPosition: Int, toPosition: Int)
        fun swipeToDelete(todo: Todo)
        fun onEmptyList()
        fun onNonEmptyList()
    }


}