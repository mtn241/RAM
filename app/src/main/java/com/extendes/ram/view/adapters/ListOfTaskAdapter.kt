package com.extendes.ram.view.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extendes.ram.database.ListItem
import com.extendes.ram.R
import kotlinx.android.synthetic.main.task_list_item.view.*

class ListOfTaskAdapter(private var listOfTask:MutableList<ListItem>):RecyclerView.Adapter<ListOfTaskAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(viewTask: View): RecyclerView.ViewHolder(viewTask)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView= LayoutInflater.from(parent.context).inflate(R.layout.task_list_item,parent,false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.apply {
            task_list_item_text.text=listOfTask[position].list_item
        }
    }

    override fun getItemCount(): Int {
        return listOfTask.size
    }
    fun removeAt(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }
    fun resetList(newList:MutableList<ListItem>){
        listOfTask=newList
    }
}