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
import com.aminook.tunemyday.util.ItemMoveCallback
import kotlinx.android.synthetic.main.todo_item.view.*

class TodoAdapter(val isSummary: Boolean = false, val currentDay: Int) :
    RecyclerView.Adapter<BaseViewHolder<Todo>>(), ItemMoveCallback {

    //private val TAG = "aminjoon"
    private var listener: ToDoRecyclerViewListener? = null
    private var _todos = mutableListOf<Todo>()
    private var _padding=0

    val padding:Int
    get() = _padding

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
        if(pos>=0){
            _todos.removeAt(pos)
            notifyItemRemoved(pos)
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
    }

    fun addItem(todo: Todo) {
        var pos=_todos.withIndex().indexOfFirst { it.value.priorityIndex>todo.priorityIndex }
        if (pos<0){
            pos= _todos.size - _padding
        }
        _todos.add(pos, todo)
        notifyItemInserted(pos)
    }


    fun submitList(list: List<Todo>?,addPadding:Boolean=false,withAddButton:Boolean=false) {
        _todos.clear()

        list?.let {
            _todos.addAll(it)
        }
        if (withAddButton){
            _todos.add(Todo(id = -1))
            _padding+=1
        }
        if (addPadding){
            _todos.add(Todo(id = -2))
            _padding+=1
        }
        notifyDataSetChanged()
    }

    fun setListener(listener: ToDoRecyclerViewListener) {
        this.listener = listener
    }

    override fun onItemSwap(fromPosition: Int, toPosition: Int) {
        if (toPosition < _todos.size - _padding && fromPosition < _todos.size - _padding) {
            listener?.swapItems(_todos[fromPosition].copy(), _todos[toPosition].copy())
        }
    }

    override fun onItemSwipe(itemPosition: Int, direction: Int) {}

    inner class ViewHolder(itemView: View) :
        BaseViewHolder<Todo>(itemView) {
        override fun bind(item: Todo) {
            if (item.id == -2L) {
                itemView.visibility = View.INVISIBLE
                return
            } else {

                itemView.txt_todo_title.text = item.title
                if (!isSummary) {
                    if (item.isDone) {
                        itemView.chk_todo.apply {
                            isChecked = true
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
                            layoutPosition
                        )
                    }
                } else {
                    itemView.chk_todo.apply {
                        setButtonDrawable(R.drawable.cb_selector_disabled)
                        isEnabled = false
                    }

                }


                itemView.txt_todo_title.setOnClickListener {
                    listener?.onEditTodoClick(item, layoutPosition)
                }
            }

        }
    }

    interface ToDoRecyclerViewListener {
        fun onEditTodoClick(todo: Todo, position: Int)
        fun onCheckChanged(todo: Todo, checked: Boolean, position: Int)
        fun swapItems(fromPosition: Todo, toPosition: Todo)
    }


}