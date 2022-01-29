package com.extendes.ram.viewmodel.handlers


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.extendes.ram.R
import com.extendes.ram.database.*


interface TaskHandlerInterface{
    fun reloadTaskData(task: TaskWithList)
    fun getParent(): TaskEntity
    fun getId():Long
    fun getName():String
    fun setName(name:String)
    fun getStart():Long
    fun setStart(date:Long)
    fun getEnd():Long
    fun setEnd(date:Long)
    fun getPriority():Int
    fun setPriority(priority: PriorityTypes)
    fun getTaskType():Int
    fun setTaskType(type: TaskTypes)
    fun getTaskAlarm():List<Pair<TaskHandler.Companion.AlarmEntries,String>>
    fun setTaskAlarm(list:List<Pair<TaskHandler.Companion.AlarmEntries,String>>)
    fun updateTaskAlarm():Boolean


    fun getList():MutableList<ListItem>
    fun packToListItem(value:String): ListItem?
    fun removeFromListAt(position: Int)
    fun insertToListAt(value: ListItem):Int


    fun getContactList():MutableList<ContactListItem>
    fun removeFromContactListAt(position: Int)
    fun insertToContactListAt(contact: ContactListItem):Int
    fun resolveInputToContact(value:String,contactType: ContactTypes): ContactListItem?


    fun notifyOnListsChanges():Boolean


    fun attachAllToId(id:Long)
    fun getManagedTask(): TaskWithList
    fun getLoadedTaskLive():LiveData<TaskWithList>
    fun taskChangedLive():LiveData<Boolean>
    fun isNewTask():Boolean
    fun getErrorMessage():String
    fun shouldCheckPlace():Boolean
    fun timeChanged():Boolean


}

class TaskHandler(/*private var */loadedTask: TaskWithList): TaskHandlerInterface {
    private val entityHandler: TaskEntityHandlerInterface = TaskEntityHandler(loadedTask.parent)
    private val listHandler: TaskFormListInterface = TaskFormList(loadedTask.list)
    private val contactListHandler: TaskFormContactListInterface = TaskFormContactList(loadedTask.contact_list)
    private val loadedTaskLive=MutableLiveData<TaskWithList>()
    init{
        loadedTaskLive.value=loadedTask
    }

    override fun updateTaskAlarm(): Boolean {
        return entityHandler.updateAlarm()
    }

    override fun reloadTaskData(task: TaskWithList) {
        entityHandler.resetManagedEntity(task.parent)
        listHandler.reloadList(task.list)
        contactListHandler.reloadContactList(task.contact_list)
        loadedTaskLive.value=task
    }

    override fun getParent(): TaskEntity {
        return entityHandler.getParent()
    }

    override fun getId(): Long {
        return entityHandler.getId()
    }

    override fun getName(): String {
        return entityHandler.getName()
    }

    override fun setName(name: String) {
        entityHandler.setName(name)
    }

    override fun getStart(): Long {
        return entityHandler.getStart()
    }

    override fun setStart(date: Long) {
        entityHandler.setStart(date)
    }

    override fun getEnd(): Long {
        return entityHandler.getEnd()
    }

    override fun setEnd(date: Long) {
        entityHandler.setEnd(date)
    }

    override fun getPriority(): Int {
        return entityHandler.getPriority()
    }

    override fun setPriority(priority: PriorityTypes) {
        entityHandler.setPriority(priority)
    }

    override fun getTaskType(): Int {
        return entityHandler.getTaskType()
    }

    override fun setTaskType(type: TaskTypes) {
        entityHandler.setTaskType(type)
    }

    override fun getTaskAlarm(): List<Pair<AlarmEntries, String>> {
        return castLongToAlarmList(entityHandler.getAlarm())
    }

    override fun setTaskAlarm(list: List<Pair<AlarmEntries, String>>) {
        entityHandler.setAlarm(castAlarmListToLong(list))
    }

    override fun getList(): MutableList<ListItem> {
        return listHandler.getList()
    }

    override fun packToListItem(value: String): ListItem? {
        return listHandler.packToListItem(value,entityHandler.getId())
    }

    override fun removeFromListAt(position: Int) {
        listHandler.removeFromListAt(position)
    }

    override fun insertToListAt(value: ListItem): Int {
         return listHandler.insertToListAt(value)
    }


    override fun getContactList(): MutableList<ContactListItem> {
        return contactListHandler.getContactList()
    }


    override fun removeFromContactListAt(position: Int) {
        contactListHandler.removeFromContactListAt(position)
    }

    override fun insertToContactListAt(contact: ContactListItem): Int {
        return contactListHandler.insertToContactListAt(contact)
    }

    override fun resolveInputToContact(value: String, contactType: ContactTypes): ContactListItem? {
        return contactListHandler.createContactItem(entityHandler.getId(),value,contactType)
    }



    override fun timeChanged(): Boolean {
       return entityHandler.timeChanged()
    }



    override fun shouldCheckPlace(): Boolean {
        return entityHandler.checkPlace()
    }

    override fun getErrorMessage(): String {
        return entityHandler.getParentErrorMessage()
    }

    override fun getLoadedTaskLive(): LiveData<TaskWithList> {
        return loadedTaskLive
    }

    override fun taskChangedLive(): LiveData<Boolean> {
        return entityHandler.entityChangedLive()
    }
    override fun notifyOnListsChanges(): Boolean {
        return listHandler.listChanged()||contactListHandler.contactListChanged()
    }
    override fun attachAllToId(id: Long) {
        entityHandler.setId(id)
        listHandler.attachToId(id)
        contactListHandler.attachToId(id)
    }

    override fun getManagedTask(): TaskWithList {
        return TaskWithList(getParent(), getContactList(), getList())
    }
    override fun isNewTask(): Boolean {
        if(entityHandler.getId()<=0){
            return true
        }
        return false
    }
    private fun castLongToAlarmList(millis:Long?):List<Pair<AlarmEntries,String>>{
        val list= mutableListOf<Pair<AlarmEntries,String>>()
        millis?.let {   alarmNotNull->
            var alarm:Long=alarmNotNull
            AlarmEntries.values().forEach {
            val rem=alarm%it.millis
            if(rem<alarm){
                val value=(alarm-rem)/it.millis
                list.add((Pair(it,value.toString())))
                alarm=rem
            }}
            if(list.isEmpty()){list.add((Pair(AlarmEntries.Minutes,"0")))}
        }
        return list
    }
    private fun castAlarmListToLong(list:List<Pair<AlarmEntries,String>>):Long?{
        if(list.isEmpty()){return null}
        var millis:Long=0
        list.forEach {
            millis+=it.second.toLong()*it.first.millis
        }
        return millis
    }
    companion object{
        enum class AlarmEntries(val millis:Long,val label:Int){
            Days(86400000, R.string.alarm_day_label),
            Hours(3600000, R.string.alarm_hour_label),
            Minutes(60000, R.string.alarm_minute_label)
        }
    }
}