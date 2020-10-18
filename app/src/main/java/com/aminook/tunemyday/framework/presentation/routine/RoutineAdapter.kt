package com.aminook.tunemyday.framework.presentation.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.model.Schedule
import com.aminook.tunemyday.framework.datasource.cache.model.RoutineEntity
import com.aminook.tunemyday.framework.presentation.common.BaseViewHolder
import kotlinx.android.synthetic.main.routine_item.view.*

class RoutineAdapter : ListAdapter<RoutineEntity, BaseViewHolder<RoutineEntity>>(DIFF_UTIL) {
    private var listener: RoutineAdapterListener? = null

    val TYPE_ROUTINE=1
    val TYPE_LAST=2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<RoutineEntity> {
        if (viewType==TYPE_ROUTINE){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_item, parent, false)
            return ViewHolder(view)
        }else{
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.last_item_empty, parent, false)
            return LastItemViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).id!=0L){
            TYPE_ROUTINE
        }else{
            TYPE_LAST
        }
    }

    override fun onBindViewHolder(holder:BaseViewHolder<RoutineEntity>, position: Int) {
        val routine = currentList[position]
        holder.bind(routine)
    }

    fun setListener(listener: RoutineAdapterListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder<RoutineEntity>(itemView) {

        override fun bind(item: RoutineEntity) {
            itemView.txt_routine_name.text = item.name

            itemView.txt_routine_name.setOnClickListener {
                listener?.onRoutineClick(item)
            }
            itemView.txt_routine_blank.setOnClickListener {
                listener?.onRoutineClick(item)
            }

            itemView.img_delete_routine.setOnClickListener {
                listener?.onDeleteRoutineClick(item)
            }
            itemView.img_edit_routine.setOnClickListener {
                listener?.onUpdateRouineClick(item)
            }

        }
    }

    inner class LastItemViewHolder(itemView: View) : BaseViewHolder<RoutineEntity>(itemView) {
        override fun bind(item: RoutineEntity) {

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