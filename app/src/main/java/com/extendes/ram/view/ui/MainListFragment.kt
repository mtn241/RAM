package com.extendes.ram.view.ui

import android.animation.AnimatorInflater
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.extendes.ram.viewmodel.model.MainViewModel
import com.extendes.ram.R
import com.extendes.ram.view.adapters.TaskListAdapter
import com.extendes.ram.database.TaskTypes
import kotlinx.android.synthetic.main.main_list_fragment.*
import java.util.*

class MainListFragment: Fragment(), TaskListAdapter.OnTaskClickListener {
    private val parentModel: MainViewModel by  activityViewModels()
    private lateinit var listAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listAdapter= TaskListAdapter(parentModel.getMainList(),parentModel::timeToMainList,this)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_list_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_list.layoutManager= LinearLayoutManager(requireContext())
        main_list.adapter=listAdapter
        parentModel.getListTypeLive().observe(viewLifecycleOwner, {
            parentModel.loadMainList()
            main_list_move_to.text=when(it){
                TaskTypes.Deadline->resources.getString(R.string.main_list_to_meetings)
                TaskTypes.Meeting->resources.getString(R.string.main_list_to_deadlines)
            }
            listAdapter.notifyDataSetChanged()
        })
        main_list_move_to.setOnClickListener {
            parentModel.switchListType()
        }
        main_bottom_menu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bottom_menu_previous -> parentModel.dayDown()
                R.id.bottom_menu_next -> parentModel.dayUp()
                R.id.bottom_menu_today ->parentModel.toToday()
                R.id.bottom_menu_jump_to ->{
                    val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        parentModel.toDate(dayOfMonth,monthOfYear,year)
                    }
                    DatePickerDialog(requireContext(),dateSetListener,
                        parentModel.getCalendar().get(Calendar.YEAR),
                        parentModel.getCalendar().get(Calendar.MONTH),
                        parentModel.getCalendar().get(Calendar.DAY_OF_MONTH)).show()
                }
            }
            true
        }

        parentModel.getLiveDate().observe(viewLifecycleOwner,{
            main_text.text=parentModel.dateAsString(it)
             AnimatorInflater.loadAnimator(context, R.animator.flip_main_date).apply {
                    setTarget(main_text)
                    start()
            }
            parentModel.loadMainList()
            listAdapter.notifyDataSetChanged()
        })

        parentModel.getActionOnListLive().observe(viewLifecycleOwner, {
            when(it.action){
                MainViewModel.Companion.ActionOnList.INSERT ->listAdapter.insertAt(it.position)
                MainViewModel.Companion.ActionOnList.DELETE ->listAdapter.removeAt(it.position)
                MainViewModel.Companion.ActionOnList.UPDATE ->listAdapter.updateAt(it.position)
                MainViewModel.Companion.ActionOnList.NONE ->{}
            }
        })

        parentModel.getLiveTimeFormat().observe(viewLifecycleOwner, {
            listAdapter.notifyDataSetChanged()
        })


        parentModel.getLiveDateFormat().observe(viewLifecycleOwner, {
            main_text.text= parentModel.getLiveDate().value?.let { it1 ->
                parentModel.dateAsString(
                    it1
                )
            }
            if(parentModel.getListTypeLive().value== TaskTypes.Deadline){
                listAdapter.notifyDataSetChanged()
            }
        } )

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT ) {
            override fun onMove(v: RecyclerView, h: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(h: RecyclerView.ViewHolder, dir: Int){
                val deletedItem=listAdapter.removeAt(h.position)
                parentModel.removeFromMainList(deletedItem)
            }
        }).attachToRecyclerView(main_list)


    }

    override fun onTaskClick(position: Int) {
       parentModel.setClickedTaskAsManaged(position)
    }
}