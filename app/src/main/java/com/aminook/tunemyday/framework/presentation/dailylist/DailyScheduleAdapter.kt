package com.aminook.tunemyday.framework.presentation.dailylist

import android.animation.LayoutTransition
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.presentation.common.BaseViewHolder
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import kotlinx.android.synthetic.main.daily_schedule_item.view.*
import kotlinx.android.synthetic.main.daily_schedule_item.view.card_view_daily
import kotlinx.android.synthetic.main.daily_schedule_item.view.layout_parent_daily
import kotlinx.android.synthetic.main.daily_schedule_item.view.recycler_schedule_todo
import kotlinx.android.synthetic.main.daily_schedule_item.view.txt_daily_program_name
import kotlinx.android.synthetic.main.daily_schedule_item.view.txt_daily_start_time
import kotlinx.android.synthetic.main.daily_schedule_item_gap.view.*
import kotlinx.android.synthetic.main.daily_schedule_last_schedule.view.*
import kotlinx.android.synthetic.main.daily_schedule_last_schedule_gap.view.*

class DailyScheduleAdapter(val context: Context, val todayIndex: Int, val currentDay: Int) :
    ListAdapter<Schedule, BaseViewHolder<Schedule>>(DIFF_CALLBACK) {

    private val TAG = "aminjoon"
    private var listener: DailyScheduleAdapterListener? = null

    private val TYPE_SCHEDULE = 1
    private val TYPE_SCHEDULE_DIFF_START = 2
    private val TYPE_LAST_SCHEDULE = 3
    private val TYPE_LAST_SCHEDULE_DIFF_START = 4
    private val TYPE_LAST_ITEM = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Schedule> {
        val layout = when (viewType) {
            TYPE_SCHEDULE_DIFF_START -> R.layout.daily_schedule_item_gap
            TYPE_LAST_SCHEDULE -> R.layout.daily_schedule_last_schedule
            TYPE_LAST_SCHEDULE_DIFF_START -> R.layout.daily_schedule_last_schedule_gap
            else -> R.layout.daily_schedule_item
        }

        val view =
            LayoutInflater.from(parent.context)
                .inflate(layout, parent, false)
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Schedule>, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
        Log.d(TAG, "onBindViewHolder: ")
        //listener?.setTodoAdapter(holder,schedule)
    }

    override fun submitList(list: List<Schedule>?) {
//        val tmp= mutableListOf<Schedule>()
//        if (!list.isNullOrEmpty()){
//            tmp.addAll(list)
//            tmp.add(Schedule(id = -1L))
//        }
        super.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {

        return if (position != currentList.size - 1) {
            if (position > 0 && currentList[position - 1].endInSec != currentList[position].startInSec) {
                TYPE_SCHEDULE_DIFF_START
            } else {
                TYPE_SCHEDULE
            }

        } else {
            if (position > 0 && currentList[position - 1].endInSec != currentList[position].startInSec) {
                TYPE_LAST_SCHEDULE_DIFF_START
            } else {
                TYPE_LAST_SCHEDULE
            }
        }
    }


    fun setListener(listener: DailyScheduleAdapterListener) {
        this.listener = listener

    }


    inner class ViewHolder(itemView: View, val viewType: Int) :
        BaseViewHolder<Schedule>(itemView) {

        val todoRecyclerView = itemView.recycler_schedule_todo
        override fun bind(item: Schedule) {
            val todoAdapter: TodoAdapter? = TodoAdapter(currentDay = currentDay)

            if (viewType == TYPE_SCHEDULE) {
                if (item.startDay != todayIndex) {
                    itemView.txt_prev_day_daily.visibility = View.VISIBLE
                    itemView.view_prev_day.visibility = View.VISIBLE
                    itemView.txt_start_of_day.visibility = View.VISIBLE
                }
            }


            if (viewType == TYPE_SCHEDULE_DIFF_START) {
                if (adapterPosition > 0) {
                    val prevSchedule = currentList[adapterPosition - 1]
                    itemView.txt_daily_prev_end_time.text = prevSchedule.endTime.toString()

                }
            }

            if (viewType == TYPE_LAST_SCHEDULE) {
                if (item.startDay != todayIndex) {
                    itemView.txt_prev_day_daily_last.visibility = View.VISIBLE
                    itemView.view_prev_day_last.visibility = View.VISIBLE
                    itemView.txt_start_of_day_last.visibility = View.VISIBLE
                }
                itemView.txt_daily_end_time.text = item.endTime.toString()

                if (item.endDay != todayIndex) {
                    itemView.txt_next_day_daily.visibility = View.VISIBLE
                    itemView.view_next_day_last.visibility = View.VISIBLE
                    itemView.txt_end_of_day_last.visibility = View.VISIBLE
                }
            }

            if (viewType == TYPE_LAST_SCHEDULE_DIFF_START) {

                itemView.txt_daily_end_time_gap.text = item.endTime.toString()
                if (adapterPosition > 0) {
                    val prevSchedule = currentList[adapterPosition - 1]
                    itemView.txt_daily_prev_end_time_last.text = prevSchedule.endTime.toString()

                }
                if (item.endDay != todayIndex) {
                    itemView.txt_next_day_daily_gap.visibility = View.VISIBLE
                    itemView.view_next_day_last_gap.visibility = View.VISIBLE
                    itemView.txt_end_of_day_last_gap.visibility = View.VISIBLE
                }
            }





            itemView.card_view_daily.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            // itemView.card_view_daily.layoutTransition.setDuration(500)
            itemView.layout_parent_daily.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            // itemView.layout_parent_daily.layoutTransition.setDuration(500)
            itemView.txt_daily_start_time.text = item.startTime.toString()

            itemView.txt_daily_program_name.text = item.program.name




            itemView.txt_daily_program_name.setOnClickListener {
                listener?.onScheduleClick(item.id)
            }

            todoAdapter?.setListener(object : TodoAdapter.ToDoRecyclerViewListener {
                override fun onAddTodoClick() {
                    listener?.onAddNoteClick(item.id, item.program.id, todoAdapter)
                }


                override fun onEditTodoClick(todo: Todo, position: Int) {
                    listener?.onEditTodoClick(todo, position, todoAdapter)
                }

                override fun onCheckChanged(todo: Todo, checked: Boolean, position: Int) {
                    listener?.onCheckChanged(todo, checked, position, todoAdapter)
                }

                override fun swapItems(fromPosition: Todo, toPosition: Todo) {
                    listener?.swapItems(fromPosition, toPosition, todoAdapter)
                }

                override fun swipeToDelete(todo: Todo, position: Int) {
                    listener?.swipeToDelete(todo, position, todoAdapter)
                }

                override fun updateTodos(todos: List<Todo>) {
                    listener?.updateTodos(todos)
                }


            })
            val layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            // val dividerItemDecoration= DividerItemDecoration(context,layoutManager.orientation)

            todoAdapter?.let {

                it.submitList(item.unfinishedTodos)

                todoRecyclerView.apply {
                    this.layoutManager = layoutManager
                    adapter = todoAdapter
                    setHasFixedSize(false)
                    isNestedScrollingEnabled = false
                    // addItemDecoration(dividerItemDecoration)
                }

                val callback = DragManageAdapter(
                    it,
                    context,
                    ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                    0
                )

                val helper = ItemTouchHelper(callback)

                helper.attachToRecyclerView(todoRecyclerView)
            }


        }


    }

    inner class EmptyScheduleViewHolder(itemView: View) : BaseViewHolder<Schedule>(itemView) {
        override fun bind(item: Schedule) {

        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {


                Log.d(
                    "aminjoon", "areContentsTheSame: ${
                        oldItem.id == newItem.id
                                && newItem.unfinishedTodos.containsAll(oldItem.unfinishedTodos)
                    }"
                )
                return oldItem.id == newItem.id
                        && newItem.unfinishedTodos.containsAll(oldItem.unfinishedTodos)
                        && oldItem.unfinishedTodos.containsAll(newItem.unfinishedTodos)


            }


        }
    }


    interface DailyScheduleAdapterListener {
        fun onAddNoteClick(scheduleId: Long, programId: Long, dailyScheduleAdapter: TodoAdapter?)
        fun onScheduleClick(scheduleId: Long)
        fun onEditTodoClick(todo: Todo, position: Int, todoAdapter: TodoAdapter?)
        fun onCheckChanged(todo: Todo, checked: Boolean, position: Int, todoAdapter: TodoAdapter?)
        fun swapItems(fromPosition: Todo, toPosition: Todo, todoAdapter: TodoAdapter?)
        fun swipeToDelete(todo: Todo, position: Int, todoAdapter: TodoAdapter?)
        fun updateTodos(todos: List<Todo>)
    }


}