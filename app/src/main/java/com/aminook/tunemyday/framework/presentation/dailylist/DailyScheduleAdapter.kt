package com.aminook.tunemyday.framework.presentation.dailylist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.business.domain.model.Todo
import com.aminook.tunemyday.framework.presentation.common.TodoAdapter
import com.aminook.tunemyday.util.DragManageAdapter
import kotlinx.android.synthetic.main.daily_schedule_item.view.*

class DailyScheduleAdapter(val context: Context) : ListAdapter<Schedule,DailyScheduleAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val TAG="aminjoon"
    private var listener: DailyScheduleAdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.daily_schedule_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)
        Log.d(TAG, "onBindViewHolder: ")
        //listener?.setTodoAdapter(holder,schedule)


    }



    fun setListener(listener: DailyScheduleAdapterListener) {
        this.listener = listener
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val todoRecyclerView = itemView.recycler_schedule_todo
        fun bind(schedule: Schedule) {
            itemView.txt_daily_start_time.text = schedule.startTime.toString()
            itemView.txt_daily_end_time.text = schedule.endTime.toString()
            itemView.txt_daily_program_name.text = schedule.program.name
            itemView.img_add_todo.setOnClickListener {
                listener?.onAddNoteClick(schedule.id,schedule.program.id,todoRecyclerView.adapter as TodoAdapter)
            }
            itemView.txt_daily_program_name.setOnClickListener {
                listener?.onScheduleClick(schedule.id)
            }
            val todoAdapter:TodoAdapter?=TodoAdapter()
            todoAdapter?.setListener(object :TodoAdapter.ToDoRecyclerViewListener{
                override fun onDeleteTodoClick(todo: Todo) {
                    listener?.onDeleteTodoClick(todo,todoAdapter)
                }

                override fun onEditTodoClick(todo: Todo) {
                   listener?.onEditTodoClick(todo,todoAdapter)
                }

                override fun onCheckChanged(todo: Todo) {
                    listener?.onCheckChanged(todo,todoAdapter)
                }

                override fun swapItems(fromPosition: Int, toPosition: Int) {
                    listener?.swapItems(fromPosition, toPosition,todoAdapter)
                }

            })
            val layoutManager=
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)

            val dividerItemDecoration= DividerItemDecoration(context,layoutManager.orientation)
            todoAdapter?.submitList(schedule.todos)

            todoAdapter?.let {
                it.submitList(schedule.todos)

                todoRecyclerView.apply {
                    this.layoutManager=layoutManager
                    adapter=todoAdapter
                    addItemDecoration(dividerItemDecoration)
                }
                val callback= DragManageAdapter(
                    it,
                    context,
                    ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
                    0
                )

                val helper=ItemTouchHelper(callback)

                helper.attachToRecyclerView(todoRecyclerView)
            }


        }



    }
    companion object{
        private val DIFF_CALLBACK= object : DiffUtil.ItemCallback<Schedule>(){
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.id==newItem.id
            }


        }
    }


    interface DailyScheduleAdapterListener {
        fun onAddNoteClick(scheduleId: Long,programId:Long, dailyScheduleAdapter: TodoAdapter)
        fun onScheduleClick(scheduleId: Long)
        fun onDeleteTodoClick(todo: Todo, todoAdapter: TodoAdapter?)
        fun onEditTodoClick(todo: Todo, todoAdapter: TodoAdapter?)
        fun onCheckChanged(todo: Todo, todoAdapter: TodoAdapter?)
        fun swapItems(fromPosition: Int, toPosition: Int, todoAdapter: TodoAdapter?)
    }
}