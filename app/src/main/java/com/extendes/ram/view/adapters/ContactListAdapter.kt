package com.extendes.ram.view.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.extendes.ram.database.ContactListItem
import com.extendes.ram.R
import kotlinx.android.synthetic.main.contact_list_item.view.*


class ContactListAdapter(private var contactList:MutableList<ContactListItem>): RecyclerView.Adapter<ContactListAdapter.ItemViewHolder>()  {
    inner class ItemViewHolder(viewTask: View): RecyclerView.ViewHolder(viewTask)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflatedView= LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item,parent,false)
        return ItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        holder.itemView.apply{
            contact_type_icon.setImageResource(contactList[position].contact_type.icon)
            contact_text_value.text=contactList[position].contact_item
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun removeAt(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }
    fun resetList(newList:MutableList<ContactListItem>){
        contactList=newList
    }
}