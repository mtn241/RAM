package com.extendes.ram.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.extendes.ram.R
import com.extendes.ram.database.TaskTypes
import kotlinx.android.synthetic.main.simple_spinner_item.view.*

class TaskTypeSpinner (val context: Context): BaseAdapter(){
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val listOfTypes: Array<TaskTypes> = TaskTypes.values()
    override fun getCount(): Int {
        return listOfTypes.size
    }
    override fun getItem(position: Int): Any {
        return  listOfTypes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.simple_spinner_item, parent, false)
        view.priority_item_text.setText(listOfTypes[position].entityName)
        view.setBackgroundResource(R.drawable.deadline_spinner_background)
        return view

    }
}