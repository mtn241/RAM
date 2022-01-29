package com.extendes.ram.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.extendes.ram.database.ContactTypes
import com.extendes.ram.R
import kotlinx.android.synthetic.main.contact_spinner_item.view.*


class ContactSpinnerAdapter(context: Context) :BaseAdapter(){
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val listOfContacts: Array<ContactTypes> = ContactTypes.values()
    override fun getCount(): Int {
        return listOfContacts.size
    }
    override fun getItem(position: Int): Any {
        return listOfContacts[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.contact_spinner_item, parent, false)
        view.contact_spinner_item_text.setText(listOfContacts[position].entityName)
        view.contact_spinner_item_icon.setImageResource(listOfContacts[position].icon)
        view.setBackgroundResource(R.drawable.contact_spinner_background)
        return view
    }
}