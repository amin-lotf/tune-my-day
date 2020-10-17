package com.aminook.tunemyday.framework.presentation.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import kotlinx.android.synthetic.main.routine_item.view.*

class RoutineAdapter : ListAdapter<RoutineEntity, RoutineAdapter.ViewHolder>(DIFF_UTIL) {
    private var listener: RoutineAdapterListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val routine = currentList[position]
        holder.bind(routine)
    }

    fun setListener(listener: RoutineAdapterListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(routine: RoutineEntity) {
            itemView.txt_routine_name.text = routine.name

            itemView.txt_routine_name.setOnClickListener {
                listener?.onRoutineClick(routine)
            }
            itemView.txt_routine_blank.setOnClickListener {
                listener?.onRoutineClick(routine)
            }

            itemView.img_delete_routine.setOnClickListener {
                listener?.onDeleteRoutineClick(routine)
            }
            itemView.img_edit_routine.setOnClickListener {
                listener?.onUpdateRouineClick(routine)
            }

        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<RoutineEntity>() {
            override fun areItemsTheSame(oldItem: RoutineEntity, newItem: RoutineEntity): Boolean {
                return newItem.id == oldItem.id
            }

            override fun areContentsTheSame(
                oldItem: RoutineEntity,
                newItem: RoutineEntity
            ): Boolean {
                return newItem.id == oldItem.id &&
                        newItem.name == oldItem.name
            }
        }
    }

    interface RoutineAdapterListener {
        fun onRoutineClick(routineEntity: RoutineEntity)
        fun onDeleteRoutineClick(routineEntity: RoutineEntity)
        fun onUpdateRouineClick(routineEntity: RoutineEntity)

    }
}