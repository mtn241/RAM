package com.extendes.ram.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.res.ResourcesCompat
import com.extendes.ram.database.PriorityTypes
import com.extendes.ram.R


import kotlinx.android.synthetic.main.simple_spinner_item.view.*

class PrioritySpinnerAdapter(val context: Context): BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val listOfPriorities: Array<PriorityTypes> = PriorityTypes.values()
    override fun getCount(): Int {
        return listOfPriorities.size
    }
    override fun getItem(position: Int): Any {
        return listOfPriorities[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.simple_spinner_item, parent, false)
        view.priority_item_text.setText(listOfPriorities[position].entityName)

        view.background=ResourcesCompat.getDrawable(context.resources,listOfPriorities[position].background,null)
        return view
    }
}