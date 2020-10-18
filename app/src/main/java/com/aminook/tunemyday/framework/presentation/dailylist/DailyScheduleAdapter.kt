package com.aminook.tunemyday.framework.presentation.dailylist

import android.animation.LayoutTransition
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.presentation.common.BaseViewHolder
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import com.aminook.tunemyday.util.setTransition
import kotlinx.android.synthetic.main.daily_schedule_item.view.*

class DailyScheduleAdapter(val context: Context) :
    ListAdapter<Schedule, BaseViewHolder<Schedule>>(DIFF_CALLBACK) {

    private val TAG = "aminjoon"
    private var listener: DailyScheduleAdapterListener? = null

    private val TYPE_Schedule = 1
    private val TYPE_LAST = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Schedule> {

        if (viewType==TYPE_Schedule) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.daily_schedule_item, parent, false)
            return ViewHolder(view)
        }else{
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.last_item_empty, parent, false)
            return LastItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Schedule>, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
        Log.d(TAG, "onBindViewHolder: ")
        //listener?.setTodoAdapter(holder,schedule)
    }

    override fun getItemViewType(position: Int): Int {
        val schedule=getItem(position)

        return if (schedule.id>0){
            TYPE_Schedule
        }else{
            TYPE_LAST
        }

    }


    fun setListener(listener: DailyScheduleAdapterListener) {
        this.listener = listener

    }


    inner class ViewHolder(itemView: View) : BaseViewHolder<Schedule>(itemView) {

        val todoRecyclerView = itemView.recycler_schedule_todo
        override fun bind(item: Schedule) {
            val todoAdapter: TodoAdapter? = TodoAdapter()
            itemView.card_view_daily.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

            itemView.txt_daily_start_time.text = item.startTime.toString()
            itemView.txt_daily_end_time.text = item.endTime.toString()
            itemView.txt_daily_program_name.text = item.program.name
            itemView.txt_upper_label_daily.setBackgroundColor(item.program.color)
            itemView.img_add_todo.setOnClickListener {
                listener?.onAddNoteClick(item.id, item.program.id, todoAdapter)
            }
            itemView.txt_daily_program_name.setOnClickListener {
                listener?.onScheduleClick(item.id)
            }

            todoAdapter?.setListener(object : TodoAdapter.ToDoRecyclerViewListener {


                override fun onEditTodoClick(todo: Todo) {
                    listener?.onEditTodoClick(todo, todoAdapter)
                }

                override fun onCheckChanged(todo: Todo) {
                    listener?.onCheckChanged(todo, todoAdapter)
                }

                override fun swapItems(fromPosition: Int, toPosition: Int) {
                    listener?.swapItems(fromPosition, toPosition, todoAdapter)
                }

                override fun swipeToDelete(todo: Todo) {
                    listener?.swipeToDelete(todo, todoAdapter)
                }

                override fun onEmptyList() {
                    itemView.txt_empty_todo.visibility = View.VISIBLE
                    todoRecyclerView.visibility = View.GONE
                }

                override fun onNonEmptyList() {
                    itemView.txt_empty_todo.visibility = View.GONE
                    todoRecyclerView.visibility = View.VISIBLE
                }

            })
            val layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            // val dividerItemDecoration= DividerItemDecoration(context,layoutManager.orientation)
            todoAdapter?.submitList(item.todos)

            todoAdapter?.let {

                it.submitList(item.todos)

                todoRecyclerView.apply {
                    this.layoutManager = layoutManager
                    adapter = todoAdapter
                    setHasFixedSize(false)
                    isNestedScrollingEnabled=false
                    // addItemDecoration(dividerItemDecoration)
                }
                itemView.layout_daily_schedule.setTransition()
                val callback = DragManageAdapter(
                    it,
                    context,
                    ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                    ItemTouchHelper.LEFT
                )

                val helper = ItemTouchHelper(callback)

                helper.attachToRecyclerView(todoRecyclerView)
            }


        }


    }

    inner class LastItemViewHolder(itemView: View) : BaseViewHolder<Schedule>(itemView) {
        override fun bind(item: Schedule) {

        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id == newItem.id
            }


        }
    }


    interface DailyScheduleAdapterListener {
        fun onAddNoteClick(scheduleId: Long, programId: Long, dailyScheduleAdapter: TodoAdapter?)
        fun onScheduleClick(scheduleId: Long)
        fun onEditTodoClick(todo: Todo, todoAdapter: TodoAdapter?)
        fun onCheckChanged(todo: Todo, todoAdapter: TodoAdapter?)
        fun swapItems(fromPosition: Int, toPosition: Int, todoAdapter: TodoAdapter?)
        fun swipeToDelete(todo: Todo, todoAdapter: TodoAdapter?)
    }


}