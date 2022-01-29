package com.extendes.ram.view.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extendes.ram.view.ui.MainActivity
import com.extendes.ram.R
import com.extendes.ram.database.TaskWithList
import kotlinx.android.synthetic.main.task_item.view.*
import kotlin.reflect.KProperty


class TaskListAdapter (
    private var taskList:MutableList<TaskWithList>,
    val resolveTime: (m: TaskWithList) -> (String),
    val taskListener: OnTaskClickListener
)
    :RecyclerView.Adapter<TaskListAdapter.TaskItemViewHolder>() {

    inner class TaskItemViewHolder(viewTask: View):RecyclerView.ViewHolder(viewTask),View.OnClickListener{
        init {
            viewTask.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position=bindingAdapterPosition
            if (position!=RecyclerView.NO_POSITION){
                taskListener.onTaskClick(position)
            }
        }
    }
    interface OnTaskClickListener{
        fun onTaskClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskItemViewHolder {
        val inflatedView=LayoutInflater.from(parent.context).inflate(R.layout.task_item,parent,false)
        return TaskItemViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: TaskItemViewHolder, position: Int) {
        holder.itemView.setBackgroundResource(taskList[position].parent.task_priority.background)
        holder.itemView.apply {
            task_item_name.text=taskList[position].parent.task_name
            task_item_time.text=resolveTime(taskList[position])
            task_item_name.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            task_item_contacts_icon.visibility=if(taskList[position].contact_list.isEmpty())View.GONE else View.VISIBLE
            task_item_list_icon.visibility=if(taskList[position].list.isEmpty())View.GONE else View.VISIBLE
        }
    }
    override fun getItemCount(): Int {
        return taskList.size
    }
    operator fun getValue(mainActivity: MainActivity, property: KProperty<*>): TaskListAdapter {
        return this
    }
    fun removeAt(position: Int): TaskWithList {
        val deleted=taskList[position]
        taskList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
        return deleted
    }
    fun insertAt(position:Int){
        notifyItemInserted(position)
        notifyItemRangeChanged(position, itemCount - position)
    }
    fun updateAt(position:Int){
        notifyItemChanged(position)
    }
}

