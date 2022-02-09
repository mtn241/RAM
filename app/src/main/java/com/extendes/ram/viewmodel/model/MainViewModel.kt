package com.extendes.ram.viewmodel.model



import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extendes.ram.App
import com.extendes.ram.database.*
import com.extendes.ram.viewmodel.handlers.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

data class AlarmData(val id:String,val message:String,val color:Int,val alarm:Long,val cancel:Boolean)

class MainViewModel :ViewModel() {
    //facade members
    private val repository: TaskRepository = TaskRepository()
    private val dateManager: DateHandlerInterface = DateHandler()
    private val formatHandler: DateFormatHandlerInterface = DateFormatHandler()
    private val taskHandler: TaskHandlerInterface
    private val listHandler: TaskListHandlerInterface = TaskListHandler(mutableListOf())
    //private members
    private var loadedTaskPosition:Int=-1 //position of task in MainList recycled view
    private val currentListType=MutableLiveData(TaskTypes.Meeting)//task type currently displayed
    private val actionOnMainList=MutableLiveData(EventOnList(ActionOnList.NONE,0))//event to MainList adapter
    private val toForm=MutableLiveData(false)//notifies when to remove/add TaskFormFragment
    private val alarmLive=MutableLiveData<AlarmData>()//cancels/ adds alarm in main activity
    private val contactLiveEvent=MutableLiveData<ContactEvent>()
    init {
        loadMainList()
        taskHandler= TaskHandler(getDefaultEntity())
    }

    private fun setActionOnList(event : EventOnList){
        actionOnMainList.value=event
    }//sets event to main list if change comes from TaskFormFragment
    private fun belongToCurrentList():Boolean{
        return isOfCurrentType()&&inCurrentDate()
    }//checks if added/updated task belong to currently displayed main list
    private fun isOfCurrentType():Boolean{
        return currentListType.value.let{
            it?.ordinal==taskHandler.getTaskType()
        }
    }
    private fun inCurrentDate():Boolean{
        if(taskHandler.getStart()<dateManager.getDateEnd()&&taskHandler.getEnd()>dateManager.getDateStart()){
            return true
        }
        return false
    }
    private fun updateTask(){
        viewModelScope.launch(Dispatchers.IO){
            repository.update(taskHandler.getParent())
        }
    } //update task in DB
    private fun insertTask(){
        val lastId =viewModelScope.async (Dispatchers.IO) {
            repository.insert(taskHandler.getParent())
        }
        runBlocking {
            taskHandler.attachAllToId(lastId.await())
        }
        viewModelScope.launch{repository.insertTaskList(taskHandler.getList())}
        viewModelScope.launch{repository.insertTaskContactList(taskHandler.getContactList())}
    } //insert task to DB
    private fun onTaskUpdate(){
        updateTask()
        if(taskHandler.timeChanged()){
            setActionOnList(EventOnList(ActionOnList.DELETE, loadedTaskPosition))
            if(inCurrentDate()){
                val position= listHandler.insertTo(taskHandler.getManagedTask())
                setActionOnList(EventOnList(ActionOnList.INSERT, minOf(position,loadedTaskPosition)))
            }
        }
        else{
            listHandler.getList()[loadedTaskPosition]=taskHandler.getManagedTask()
            setActionOnList(EventOnList(ActionOnList.UPDATE, loadedTaskPosition))
        }
    }//updates DB and MainList if needed
    private fun onTaskInsert(){
        insertTask()
        if(belongToCurrentList()){
            val position=listHandler.insertTo(taskHandler.getManagedTask())
            setActionOnList(EventOnList(ActionOnList.INSERT,position))
        }
    }//same as above
    private fun setManagedTask(task: TaskWithList =getDefaultEntity(), position:Int=-1){
        loadedTaskPosition=position
        taskHandler.reloadTaskData(task)
    }
    private fun getDefaultEntity(): TaskWithList {
        val timeAtInvoke:Long= dateManager.dateToLong(dateManager.trimSeconds(Date()))
        val defaultEntity = TaskEntity(
            0, "",
            timeAtInvoke,
            timeAtInvoke,
            TaskTypes.Deadline, PriorityTypes.Lowest,null
        )
        return TaskWithList(defaultEntity, mutableListOf(), mutableListOf())
    } //gets blank task for TaskHandler (add new case)
    private fun getFormTimeText(date:Date):String{
        return formatHandler.timeToString(date)+"\n"+dateAsString(date)
    } //returns date as string for START/END buttons in TaskFormFragment
    private fun getAlarmStart(task: TaskEntity):Long{
        task.task_alarm?.let {
            return when(task.task_type){
                TaskTypes.Deadline->task.task_end-it
                TaskTypes.Meeting->task.task_start-it
            }
        }
        return 0
    }//returns millis for alarm start
    private fun setLiveAlarmData(task: TaskEntity, cancel:Boolean){
        val start=getAlarmStart(task)
        val cancelMe:Boolean=if(start<=0) true else cancel
        alarmLive.value= AlarmData(task.task_id.toString(),setAlarmNotificationMessage(task,start),task.task_priority.color,start,cancelMe)
    }
    private fun setAlarmNotificationMessage(task: TaskEntity, start:Long):String{
        return task.task_name+"\n"+ App.instance.resources.getString(task.task_type.alarm)+" "+getAlarmTimeMessage(task,start)
    }
    private fun getAlarmTimeMessage(task: TaskEntity, start:Long):String{
        val end=when(task.task_type){
            TaskTypes.Deadline->task.task_end
            TaskTypes.Meeting->task.task_start
        }
        val endAsDate=dateManager.longToDate(end)
        var message:String=formatHandler.timeToString(endAsDate)
        if(!dateManager.inSameDay(start,end)){
            message+=" "+formatHandler.dateToString(endAsDate)}
        return message
    }
    private fun goToForm(){
        toForm.value=true
    }
    //date handler
    fun dayUp(){
        dateManager.dayUp()
    }//date travers
    fun dayDown(){
        dateManager.dayDown()
    }//...
    fun toToday(){
        dateManager.today()
    }//...
    fun toDate(day:Int,month:Int,year:Int){
        dateManager.setDate(day,month,year)
    }//...
    fun getCalendar():Calendar{
        return dateManager.getCalendar()
    }//calendar to set current date in DatePicker
    fun getLiveDate():LiveData<Date>{
        return dateManager.getLiveDate()
    }//on change loads up to date MainList
    //alarm updates
    fun getLiveAlarmData():LiveData<AlarmData>{
        return alarmLive
    } //force update of alarm in main activity
    fun onWakeOnTaskNotification(id:Long){
        viewModelScope.launch {
            val task= withContext(Dispatchers.Default) {
                repository.getTaskById(id)
            }
            if(task!=null){
                dateManager.setDateOnWake(task.parent.task_start)
                currentListType.value=task.parent.task_type
                val position=listHandler.getPositionById(task)
                setClickedTaskAsManaged(position)
            }
        }
    }//populates with relevant data on TaskNotification click if task deleted before notification pressed opens app


    //entity
    fun getFormName():String{
        return taskHandler.getName()
    }
    fun setFormName(newName:String){
        taskHandler.setName(newName)
    }
    fun getFormStart():Date{
        return dateManager.longToDate(taskHandler.getStart())
    }//for datePicker
    fun getFormStartText():String{
        return getFormTimeText(dateManager.longToDate(taskHandler.getStart()))
    } //for start button text
    fun setFormStart(date:Date){
        taskHandler.setStart(dateManager.dateToLong(dateManager.trimSeconds(date)))
    }
    fun getFormEnd():Date{
        return dateManager.longToDate(taskHandler.getEnd())
    }
    fun getFormEndText():String{
        return getFormTimeText(dateManager.longToDate(taskHandler.getEnd()))
    }
    fun setFormEnd(date:Date){
        taskHandler.setEnd(dateManager.dateToLong(dateManager.trimSeconds(date)))
    }
    fun getFormPriorityPosition():Int{
        return taskHandler.getPriority()
    }
    fun setFormPriorityType(priority: PriorityTypes){
        taskHandler.setPriority(priority)
    }
    fun getFormTaskTypePosition():Int{
        return taskHandler.getTaskType()
    }
    fun setFormTaskType(type: TaskTypes){
        taskHandler.setTaskType(type)
    }
    fun setFormAlarm(list:List<Pair<TaskHandler.Companion.AlarmEntries,String>>){
        taskHandler.setTaskAlarm(list)
    }
    fun getFormAlarm():List<Pair<TaskHandler.Companion.AlarmEntries,String>>{
        return taskHandler.getTaskAlarm()
    }
    fun getFormValidationMessage():String{
        return taskHandler.getErrorMessage()
    }//returns error message for task
    fun getFormTaskChanged():LiveData<Boolean>{
        return taskHandler.taskChangedLive()
    }//hide/show update button + onBack if changed ask if to discard


    //format handler
    fun getLiveDateFormat():LiveData<SimpleDateFormat>{
        return formatHandler.getLiveDateFormat()
    }//force MainList update on date format change
    fun getLiveTimeFormat():LiveData<SimpleDateFormat>{
        return formatHandler.getLiveTimeFormat()
    }//force MainList update on time format change
    fun getLive24HourShow():LiveData<Boolean>{
        return formatHandler.display24Hour()
    }//on change sets mode for TimePickers
    fun setDateFormat(pattern:String){
        formatHandler.setDatePattern(pattern)
    }
    fun setTimeFormat(pattern:String){
        formatHandler.setTimePattern(pattern)
    }
    fun dateAsString(input:Date):String{
        return formatHandler.dateToString(input)
    }//
    fun timeToMainList(input: TaskWithList):String{
        return when(input.parent.task_type){
            TaskTypes.Deadline->formatHandler.dateToString(dateManager.longToDate(input.parent.task_end))+"\n"+formatHandler.timeToString(dateManager.longToDate(input.parent.task_end))+" "
            TaskTypes.Meeting->formatHandler.timeToString(dateManager.longToDate(input.parent.task_start))+" - "+formatHandler.timeToString(dateManager.longToDate(input.parent.task_end))+" "
        }
    }//injected to Main List adapter for proper time/date display based on TaskType of list item

    //entity lists
    fun getFormList():MutableList<ListItem>{
        return taskHandler.getList()
    }
    fun removeFromFormList(position: Int){
        val itemDeleted=taskHandler.getItemAt(position)
        if(!taskHandler.isNewTask()){
            viewModelScope.launch(Dispatchers.IO) { repository.deleteListItem(itemDeleted)}
        }
        taskHandler.removeFromListAt(position)
    }
    fun insetToFormList(value:String):Int{
        val newItem=taskHandler.packToListItem(value)?:return -1
        if(!taskHandler.isNewTask()){
            viewModelScope.launch(Dispatchers.IO) {  repository.insertTaskListItem(newItem)}
        }
        return taskHandler.insertToListAt(newItem)
    }
    fun getFormContactList():MutableList<ContactListItem>{
        return taskHandler.getContactList()
    }
    fun removeFromFormContactList(position: Int){
        val itemDeleted=taskHandler.getContactList()[position]
        if(!taskHandler.isNewTask()){
            viewModelScope.launch(Dispatchers.IO) { repository.deleteContactItem(itemDeleted)}
        }
        taskHandler.removeFromContactListAt(position)
    }
    fun insetToFormContactList(value: String, contactType: ContactTypes):Int{
        val newItem= taskHandler.resolveInputToContact(value,contactType) ?: return -1
        if(!taskHandler.isNewTask()){
            viewModelScope.launch(Dispatchers.IO) {  repository.insertTaskContactListItem(newItem)}
        }
        return taskHandler.insertToContactListAt(newItem)
    }
    fun onContactItemClick(position: Int){
        val event=packToContactEvent(taskHandler.getContactAt(position))
        event?.let {
            contactLiveEvent.value=it
        }
    }
    fun getContactEventLive():LiveData<ContactEvent> = contactLiveEvent
    private fun packToContactEvent(contact:ContactListItem):ContactEvent?{
        return when(contact.contact_type){
            ContactTypes.MAIL->ContactEvent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+contact.contact_item) )
            ContactTypes.PHONE->ContactEvent(Intent.ACTION_DIAL,Uri.parse("tel:"+contact.contact_item))
            else->null
        }
    }

    //task level
    fun getTaskPlaceValidation():String {
        var message=""
        if(!taskHandler.shouldCheckPlace()){return message}
        var result: List<TaskEntity>
        runBlocking{
            result = if(taskHandler.isNewTask()){
                withContext(Dispatchers.Default) { repository.checkPlaceForMeeting(taskHandler.getStart(), taskHandler.getEnd())
                }
            } else{
                withContext(Dispatchers.Default) { repository.checkPlaceForMeetingUpdate(taskHandler.getStart(), taskHandler.getEnd(), taskHandler.getId())
                }
            }
        }
        result.forEach{
            message+="${it.task_name} ${formatHandler.timeToString(dateManager.longToDate(it.task_start))} - ${formatHandler.timeToString(dateManager.longToDate(it.task_end))}\n"
        }
        return message
    }//checks if there is place for new meeting (returns list of meetings with conflict as string)
    fun getLoadedTaskLive():LiveData<TaskWithList>{
        return taskHandler.getLoadedTaskLive()
    } //force update of fields in TaskFormFragment (if add new pressed in active TaskFormFragment)
    fun isNewTask():Boolean{
        return taskHandler.isNewTask()
    }
    fun setClickedTaskAsManaged(position: Int) {
        setManagedTask(listHandler.getTaskAt(position),position)
        goToForm()
    }
    fun setNewAsManaged(){
        setManagedTask()
        goToForm()//temp
    }

    //main list
    fun getListTypeLive():LiveData<TaskTypes>{
        return currentListType
    }//load main list on task type change
    fun getMainList():MutableList<TaskWithList>{
        return listHandler.getList()
    }
    fun switchListType(){
        currentListType.value?.let {
            currentListType.value=when(it){
                TaskTypes.Meeting-> TaskTypes.Deadline
                TaskTypes.Deadline-> TaskTypes.Meeting
            }
        }
    }
    fun getActionOnListLive():LiveData<EventOnList>{
        return actionOnMainList
    } //indirect event to mainListFragment to update recycled view list
    fun loadMainList(){
        currentListType.value?.let {
            runBlocking{
                val newList=when(it){
                    TaskTypes.Deadline->viewModelScope.async  (Dispatchers.IO){repository.getAllDeadlines(dateManager.getDateStart(), dateManager.getDateEnd())}
                    TaskTypes.Meeting-> viewModelScope.async  (Dispatchers.IO){repository.getAllMeetings(dateManager.getDateStart(), dateManager.getDateEnd())}
                }
                listHandler.setList(newList.await() as MutableList<TaskWithList>)
            }
        }
    }
    fun onFormListChanged(){
        if(!isNewTask()&&taskHandler.notifyOnListsChanges()){
            setActionOnList(EventOnList(ActionOnList.UPDATE,loadedTaskPosition))
        }
    }//adds icons to Task item in main list if any of lists added or removed
    fun toFormLive():LiveData<Boolean>{
        return toForm
    }//adds/removes TaskFormFragment based on value
    fun returnFromForm(){
        toForm.value=false
    }

    //DB + alarm updates
    fun removeFromMainList(entity: TaskWithList){
        setLiveAlarmData(entity.parent,true)
        runBlocking{
            repository.clearTaskContactList(entity.parent.task_id)
            repository.clearTaskList(entity.parent.task_id)
            repository.delete(entity.parent)
        }
    }//removes task from DB + cancels alarm
    fun updateInsertTask(){
        if (taskHandler.isNewTask()){
            onTaskInsert()
        }
        else{
           onTaskUpdate()
        }
       if(taskHandler.updateTaskAlarm()){
           setLiveAlarmData(taskHandler.getParent(),false)
       }
    }// updates/inserts task to DB + adds alarm
    fun cleanOutdatedTasks(){
        viewModelScope.launch (Dispatchers.IO){
            repository.deleteOutDated(dateManager.getDateStart())
        }
    } //clears DB from tasks beyond today
    companion object{
        enum class ActionOnList {
            NONE,
            INSERT,
            UPDATE,
            DELETE,

        }
        data class EventOnList(var action: ActionOnList, var position:Int)
        data class ContactEvent(val action:String,val data:Uri)
    }
}