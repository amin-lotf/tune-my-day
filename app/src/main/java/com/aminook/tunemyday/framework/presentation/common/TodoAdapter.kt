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


    fun updateItem(todo: Todo, position: Int) {
        _todos[position] = todo
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        _todos.removeAt(position)
        notifyItemRemoved(position)

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

    fun addItem(todo: Todo, position: Int = _todos.size - 2) {
        _todos.add(position, todo)
        notifyItemInserted(position)
    }


    fun submitList(list: List<Todo>?) {
        val tmp = list as MutableList
        tmp.add(Todo(id = -1))
        tmp.add(Todo(id = -2))
        _todos = tmp
        notifyDataSetChanged()
    }

    fun setListener(listener: ToDoRecyclerViewListener) {
        this.listener = listener
    }


    override fun onItemSwap(fromPosition: Int, toPosition: Int) {
        if (toPosition < _todos.size - 2 && fromPosition < _todos.size - 2) {
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