package com.aminook.tunemyday.framework.presentation.common

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.datasource.cache.database.TodoDao
import com.aminook.tunemyday.util.ItemMoveCallback
import kotlinx.android.synthetic.main.todo_item.view.*
import javax.inject.Inject

class TodoAdapter(val isSummary: Boolean = false, val currentDay: Int) :
    RecyclerView.Adapter<BaseViewHolder<Todo>>(), ItemMoveCallback {

    private val TAG = "aminjoon"
    private var listener: ToDoRecyclerViewListener? = null
    private var _todos = mutableListOf<Todo>()
    private var padding=0

    val currentList: List<Todo>
        get() = _todos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Todo> {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }


    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Todo>, position: Int) {
        val todo = _todos[position]
        holder.bind(todo)
    }

    override fun getItemCount() = _todos.size


    fun updateItem(todo: Todo) {
        val pos=_todos.withIndex().indexOfFirst { it.value.id==todo.id }
        _todos[pos] = todo
        notifyItemChanged(pos)
    }

    fun removeItem(todo:Todo) {

        val pos=_todos.withIndex().indexOfFirst { it.value.id==todo.id }
        Log.d(TAG, "removeItem: $pos")
        if(pos>=0){
            _todos.removeAt(pos)
            notifyItemRemoved(pos)
        }else{
            Log.d(TAG, "removeItem: not in list")
        }


    }

    fun moveItem(todos: List<Todo>) {

        val srcIndex = _todos.withIndex().indexOfFirst { it.value.id == todos.first().id }
        val destIndex = _todos.withIndex().indexOfFirst { it.value.id == todos.last().id }

        _todos.removeAt(srcIndex)
        _todos.add(srcIndex, todos.last())

        _todos.removeAt(destIndex)
        _todos.add(destIndex, todos.first())
        notifyItemMoved(srcIndex, destIndex)


        // listener?.updateTodos(_todos)
    }

    fun addItem(todo: Todo) {
        var pos=_todos.withIndex().indexOfFirst { it.value.priorityIndex>todo.priorityIndex }
        if (pos<0){
            pos= _todos.size - padding
        }
        _todos.add(pos, todo)
        notifyItemInserted(pos)
    }


    fun submitList(list: List<Todo>?,addPadding:Boolean=true,withAddButton:Boolean=true) {
        _todos.clear()

        list?.let {
            _todos.addAll(it)
        }
        Log.d(TAG, "submitList: size: ${_todos.size}")

        if (withAddButton){
            _todos.add(Todo(id = -1))
            padding+=1
        }
        if (addPadding){
            _todos.add(Todo(id = -2))
            padding+=1
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: ToDoRecyclerViewListener) {
        this.listener = listener
    }




    override fun onItemSwap(fromPosition: Int, toPosition: Int) {
        if (toPosition < _todos.size - padding && fromPosition < _todos.size - padding) {
            listener?.swapItems(_todos[fromPosition].copy(), _todos[toPosition].copy())
        }
    }

    override fun onItemSwipe(itemPosition: Int, direction: Int) {
        listener?.swipeToDelete(_todos[itemPosition], itemPosition)
    }


    inner class ViewHolder(itemView: View) :
        BaseViewHolder<Todo>(itemView) {
        override fun bind(item: Todo) {
            if (item.id == -2L) {
                itemView.visibility = View.INVISIBLE
                return
            }

            if (item.id == -1L) {
                itemView.layout_add_todo.visibility = View.VISIBLE
                itemView.layout_show_todo.visibility = View.GONE

                itemView.setOnClickListener {
                    listener?.onAddTodoClick()
                }

            } else {

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
                            item,
                            itemView.chk_todo.isChecked,
                            adapterPosition
                        )
                    }
                } else {
                    itemView.chk_todo.isEnabled = false
                }


                itemView.txt_todo_title.setOnClickListener {
                    listener?.onEditTodoClick(item, adapterPosition)
                }
            }

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
        fun onAddTodoClick()
        fun onEditTodoClick(todo: Todo, position: Int)
        fun onCheckChanged(todo: Todo, checked: Boolean, position: Int)
        fun swapItems(fromPosition: Todo, toPosition: Todo)
        fun swipeToDelete(todo: Todo, position: Int)
        fun updateTodos(todos: List<Todo>)

    }


}