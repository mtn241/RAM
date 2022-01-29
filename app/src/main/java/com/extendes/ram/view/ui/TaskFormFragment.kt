package com.extendes.ram.view.ui

import android.content.Context
import android.os.Bundle
import android.text.InputType.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.extendes.ram.viewmodel.model.MainViewModel
import com.extendes.ram.R
import com.extendes.ram.view.adapters.*
import com.extendes.ram.database.ContactTypes
import com.extendes.ram.database.PriorityTypes
import com.extendes.ram.database.TaskTypes
import com.extendes.ram.database.TaskWithList
import com.extendes.ram.viewmodel.handlers.TaskHandler
import kotlinx.android.synthetic.main.date_time_picker.*
import kotlinx.android.synthetic.main.date_time_picker.view.*
import kotlinx.android.synthetic.main.error_alert_dialog.*
import kotlinx.android.synthetic.main.fragment_task_form.*
import kotlinx.android.synthetic.main.task_form_alarm_setter.*
import java.text.SimpleDateFormat
import java.util.*


class TaskFormFragment : Fragment() {

    private val parentModel: MainViewModel by  activityViewModels()
    private lateinit var dateTimeView:View
    private lateinit var dateTimeDialog:AlertDialog
    private lateinit var alarmView:View
    private lateinit var alarmDialog:AlertDialog
    private lateinit var errorView: View
    private lateinit var errorDialog:AlertDialog
    private lateinit var adapterContacts: ContactListAdapter
    private lateinit var adapterList: ListOfTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateTimeView = View.inflate(activity, R.layout.date_time_picker, null)
        dateTimeDialog = AlertDialog.Builder(requireContext()).create()
        dateTimeDialog.setView(dateTimeView)

        alarmView= View.inflate(activity, R.layout.task_form_alarm_setter, null)
        alarmDialog = AlertDialog.Builder(requireContext()).create()
        alarmDialog .setView(alarmView)

        errorView= View.inflate(activity, R.layout.error_alert_dialog, null)
        errorDialog = AlertDialog.Builder(requireContext()).create()
        errorDialog.setView(errorView)
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        task_form_spinner_priority.adapter= PrioritySpinnerAdapter(requireContext())
        task_form_spinner_task_type.adapter= TaskTypeSpinner(requireContext())
        task_form_spinner_contact_type.adapter= ContactSpinnerAdapter(requireContext())
        adapterContacts= ContactListAdapter(parentModel.getFormContactList())
        task_form_contact_list.adapter=adapterContacts
        task_form_contact_list.layoutManager= LinearLayoutManager(context)
        val contactsObserver=object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart:Int, itemCount:Int){
                parentModel.removeFromFormContactList(positionStart)
            }
        }
        adapterContacts.registerAdapterDataObserver(contactsObserver)

        parentModel.getLive24HourShow().observe(viewLifecycleOwner, {
            dateTimeView.date_time_picker_time.setIs24HourView(it)
        })



        adapterList= ListOfTaskAdapter(parentModel.getFormList())
        task_form_list.adapter=adapterList
        task_form_list.layoutManager= LinearLayoutManager(context)
        val listObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart:Int, itemCount:Int){
                parentModel.removeFromFormList(positionStart)
            }
        }
        adapterList.registerAdapterDataObserver(listObserver)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT ) {
            override fun onMove(v: RecyclerView, h: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(h: RecyclerView.ViewHolder, dir: Int) = adapterList.removeAt(h.bindingAdapterPosition)
        }).attachToRecyclerView(task_form_list)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT ) {
            override fun onMove(v: RecyclerView, h: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(h: RecyclerView.ViewHolder, dir: Int) = adapterContacts.removeAt(h.bindingAdapterPosition)
        }).attachToRecyclerView(task_form_contact_list)

        val timeFormatObserver= Observer<SimpleDateFormat> {
            task_form_btn_start.text=parentModel.getFormStartText()
            task_form_btn_end.text=parentModel.getFormEndText()
        }
        parentModel.getLiveTimeFormat().observe(viewLifecycleOwner,timeFormatObserver)
        val dateFormatObserver= Observer<SimpleDateFormat>{
            task_form_btn_start.text=parentModel.getFormStartText()
            task_form_btn_end.text=parentModel.getFormEndText()
        }
        parentModel.getLiveDateFormat().observe(viewLifecycleOwner,dateFormatObserver)

        val loadedTaskObserver= Observer<TaskWithList>{
            onInitFragment()
        }
        parentModel.getLoadedTaskLive().observe(viewLifecycleOwner,loadedTaskObserver)


        val taskChangedObserver=Observer<Boolean>{
            task_form_save_btn.visibility=if(it) View.VISIBLE else View.GONE
        }
        parentModel.getFormTaskChanged().observe(viewLifecycleOwner,taskChangedObserver)
        task_form_input_name.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                task_form_input_name.setText(parentModel.getFormName())
                hideKeyboard()
            }
        }



        task_form_input_name.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                parentModel.setFormName(textView.text.toString())
                task_form_input_name.clearFocus()
                task_form_layout.requestFocus()
                hideKeyboard()
            }
            false
        }
        task_form_spinner_priority.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    parentModel.setFormPriorityType(parent.getItemAtPosition(position) as PriorityTypes)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        task_form_spinner_task_type.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    parentModel.setFormTaskType(parent.getItemAtPosition(position) as TaskTypes)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        task_form_spinner_contact_type.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (parent != null) {
                    task_form_input_contact.inputType=when(position){
                        0->TYPE_CLASS_PHONE
                        1-> TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        else->TYPE_CLASS_TEXT
                    }
                    task_form_input_contact.setTextColor(resources.getColor(R.color.task_name))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        task_form_manage.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.task_form_manage_no ->{
                    noListVisibility()
                }
                R.id.task_form_manage_contacts ->{
                    contactsVisibility()
                }
                R.id.task_form_manage_list ->{
                    listVisibility()
                }
            }
        }

        task_form_list_add.setOnClickListener {
            val inputToList=task_form_list_input.text.toString()
            val atPosition=parentModel.insetToFormList(inputToList)
            if(atPosition>=0){
                adapterList.notifyItemInserted(atPosition)
                adapterList.notifyItemRangeChanged(atPosition, adapterList.itemCount - atPosition)
                task_form_list_input.text.clear()
            }
            hideKeyboard()
        }

        task_form_contact_add.setOnClickListener {
            val inputToList=task_form_input_contact.text.toString()
            val atPosition=parentModel.insetToFormContactList(inputToList,task_form_spinner_contact_type.selectedItem as ContactTypes)
            if(atPosition<0){
                task_form_input_contact.setTextColor(resources.getColor(R.color.form_wrong_input))
            }
            else{
                adapterContacts.notifyItemInserted(atPosition)
                adapterContacts.notifyItemRangeChanged(atPosition, adapterContacts.itemCount - atPosition)
                task_form_input_contact.text.clear()
                task_form_input_contact.setTextColor(resources.getColor(R.color.task_name))
            }
            hideKeyboard()
        }
        dateTimeView.date_time_picker_cancel.setOnClickListener {
            dateTimeDialog.dismiss()
        }
        task_form_btn_start.setOnClickListener {
            dateTimeDialog.show()
            dateTimeDialog.date_time_picker_header.text=task_form_start_label.text
            setDateTimePicker(parentModel.getFormStart())
            dateTimeDialog.date_time_picker_ok.setOnClickListener{
                parentModel.setFormStart(getDateTimePickerVal())
                task_form_btn_start.text=parentModel.getFormStartText()
                dateTimeDialog.dismiss()
            }

        }
        task_form_btn_end.setOnClickListener {
            dateTimeDialog.show()
            dateTimeDialog.date_time_picker_header.text=task_form_end_label.text
            setDateTimePicker(parentModel.getFormEnd())
            dateTimeDialog.date_time_picker_ok.setOnClickListener{
                parentModel.setFormEnd(getDateTimePickerVal())
                task_form_btn_end.text=parentModel.getFormEndText()
                dateTimeDialog.dismiss()
            }
        }

        task_form_remind_btn.setOnClickListener {
           alarmDialog.show()
           alarmDialog.alarm_header.text=when(parentModel.getFormTaskTypePosition()){
               0->getString(R.string.alarm_header_deadline)
               1->getString(R.string.alarm_header_meeting)
               else->""
           }
           setAlarmData(parentModel.getFormAlarm())
           alarmDialog.alarm_btn_set.setOnClickListener {
               parentModel.setFormAlarm(getAlarmData())
               setAlarmMessage(getAlarmData())
               alarmDialog.dismiss()
           }
           alarmDialog.alarm_btn_cancel.setOnClickListener {
                alarmDialog.dismiss()
           }
        }

        task_form_save_btn.setOnClickListener {
            var message=parentModel.getFormValidationMessage()
            if(message==""){
                message=parentModel.getTaskPlaceValidation()
                if(message==""){
                    parentModel.updateInsertTask()
                    parentModel.returnFromForm()
                }
                else{
                    errorDialog(R.string.task_form_invalid_time,message)
                }
            }
            else{
                errorDialog(R.string.task_form_invalid_task,message)
            }
        }
        onInitFragment()

    }
    private fun errorDialog(title:Int, message:String){
        errorDialog.show()
        errorDialog.error_cancel.setOnClickListener {
            parentModel.returnFromForm()
            errorDialog.dismiss()
        }
        errorDialog.error_repair.setOnClickListener {
            errorDialog.dismiss()
        }
        errorDialog.error_header.text=requireContext().resources.getText(title)
        errorDialog.error_message.text=message


    }


    override fun onResume() {
        super.onResume()
        onInitFragment()
    }
    private fun hideKeyboard(){
        val current=activity?.currentFocus
        if(current!=null){
            val inputManager=context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(current.windowToken,0)
        }
    }

    private fun hideOnList(hide:Boolean){
        val visible=if(hide) View.GONE else View.VISIBLE
        task_form_alarm_message.visibility=visible
        task_form_remind_btn.visibility=visible
        task_form_btn_start.visibility=visible
        task_form_btn_end.visibility=visible
        task_form_end_label.visibility=visible
        task_form_start_label.visibility=visible
        task_form_spinner_priority.visibility=visible
        task_form_spinner_task_type.visibility=visible
        task_form_manage_radio_label.visibility=visible
    }

    private fun getAlarmData():List<Pair<TaskHandler.Companion.AlarmEntries,String>>{
        val alarmList= mutableListOf<Pair<TaskHandler.Companion.AlarmEntries,String>>()
        if(alarmDialog.alarm_spinner_day.selectedItemPosition!=0){
            alarmList.add(Pair(TaskHandler.Companion.AlarmEntries.Days,alarmDialog.alarm_spinner_day.selectedItem.toString()))
        }
        if(alarmDialog.alarm_spinner_hour.selectedItemPosition!=0){
            alarmList.add(Pair(TaskHandler.Companion.AlarmEntries.Hours,alarmDialog.alarm_spinner_hour.selectedItem.toString()))
        }
        if(alarmDialog.alarm_spinner_minute.selectedItemPosition!=0){
            alarmList.add(Pair(TaskHandler.Companion.AlarmEntries.Minutes,alarmDialog.alarm_spinner_minute.selectedItem.toString()))
        }
        return alarmList
    }
    private fun setAlarmData(alarm:List<Pair<TaskHandler.Companion.AlarmEntries,String>>){
        alarm.forEach {
            when(it.first){
                TaskHandler.Companion.AlarmEntries.Days->alarmDialog.alarm_spinner_day.setSelection(it.second.toInt()+1)
                TaskHandler.Companion.AlarmEntries.Hours->alarmDialog.alarm_spinner_hour.setSelection(it.second.toInt()+1)
                TaskHandler.Companion.AlarmEntries.Minutes->alarmDialog.alarm_spinner_minute.setSelection(it.second.toInt()+1)
            }
        }
    }
    private fun getDateTimePickerVal():Date{
        val pickerDate=Calendar.getInstance()
        pickerDate.set(
            dateTimeView.date_time_picker_date.year,
            dateTimeView.date_time_picker_date.month,
            dateTimeView.date_time_picker_date.dayOfMonth,
            dateTimeView.date_time_picker_time.hour,
            dateTimeView.date_time_picker_time.minute,
            0,
        )
        return pickerDate.time
    }
    private fun setAlarmMessage(list:List<Pair<TaskHandler.Companion.AlarmEntries,String>>){
        var message=""
        list.forEach {
            if(it.second!="0"){
                message+=resources.getString(it.first.label)+":"+it.second+ " "
            }
        }
        if(list.isNotEmpty() &&message==""){
            message+=resources.getString(TaskHandler.Companion.AlarmEntries.Minutes.label)+": 0"
        }
        task_form_alarm_message.text=message
    }
    private fun setDateTimePicker(input:Date){
        val endDate=Calendar.getInstance()
        endDate.time=input
        dateTimeDialog.date_time_picker_date.updateDate(endDate.get(Calendar.YEAR),endDate.get(Calendar.MONTH),endDate.get(Calendar.DAY_OF_MONTH))
        dateTimeDialog.date_time_picker_time.hour=endDate.get(Calendar.HOUR_OF_DAY)
        dateTimeDialog.date_time_picker_time.minute=endDate.get(Calendar.MINUTE)
    }

    private fun onInitFragment(){
        setAlarmMessage(parentModel.getFormAlarm())
        task_form_input_contact.text.clear()
        task_form_input_contact.setTextColor(resources.getColor(R.color.task_name))
        task_form_list_input.text.clear()
        task_form_spinner_contact_type.setSelection(0)
        task_form_spinner_task_type.visibility=if(parentModel.isNewTask()) View.VISIBLE else View.GONE
        task_form_save_btn.text=if(parentModel.isNewTask()) getString(R.string.task_form_add_new_btn) else getString(
            R.string.task_form_update_all_btn
        )
        adapterContacts.resetList(parentModel.getFormContactList())
        adapterList.resetList(parentModel.getFormList())
        task_form_input_name.setText(parentModel.getFormName())
        task_form_btn_start.text=parentModel.getFormStartText()
        task_form_btn_end.text=parentModel.getFormEndText()
        task_form_spinner_priority.setSelection(parentModel.getFormPriorityPosition())
        task_form_spinner_task_type.setSelection(parentModel.getFormTaskTypePosition())
        task_form_manage.check(R.id.task_form_manage_no)
    }
    private fun listVisibility(){
        hideOnList(true)
        task_form_manage_no.text=getString(R.string.task_form_manage_done)
        task_form_input_contact.visibility=View.GONE
        task_form_contact_add.visibility=View.GONE
        task_form_contact_list.visibility=View.GONE
        task_form_spinner_contact_type.visibility=View.GONE
        task_form_list_input.visibility=View.VISIBLE
        task_form_list_add.visibility=View.VISIBLE
        task_form_list.visibility=View.VISIBLE
    }
    private fun contactsVisibility(){
        hideOnList(true)
        task_form_manage_no.text=getString(R.string.task_form_manage_done)
        task_form_input_contact.visibility=View.VISIBLE
        task_form_contact_add.visibility=View.VISIBLE
        task_form_contact_list.visibility=View.VISIBLE
        task_form_spinner_contact_type.visibility=View.VISIBLE
        task_form_list_input.visibility=View.GONE
        task_form_list_add.visibility=View.GONE
        task_form_list.visibility=View.GONE
    }
    private fun noListVisibility(){
        hideOnList(false)
        task_form_manage_no.text=getString(R.string.task_form_manage_no)
        task_form_input_contact.visibility=View.GONE
        task_form_contact_add.visibility=View.GONE
        task_form_contact_list.visibility=View.GONE
        task_form_spinner_contact_type.visibility=View.GONE
        task_form_list_input.visibility=View.GONE
        task_form_list_add.visibility=View.GONE
        task_form_list.visibility=View.GONE
    }

}

