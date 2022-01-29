package com.extendes.ram.viewmodel.handlers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.extendes.ram.App
import com.extendes.ram.R
import com.extendes.ram.database.PriorityTypes
import com.extendes.ram.database.TaskEntity
import com.extendes.ram.database.TaskTypes


interface TaskEntityHandlerInterface{
    fun resetManagedEntity(entity: TaskEntity)
    fun getParent(): TaskEntity
    fun setId(id:Long)
    fun getId():Long
    fun getStart():Long
    fun setStart(date:Long)
    fun getEnd():Long
    fun setEnd(date:Long)
    fun getName():String
    fun setName(name:String)
    fun getPriority():Int
    fun setPriority(priority: PriorityTypes)
    fun getTaskType():Int
    fun setTaskType(type: TaskTypes)
    fun setAlarm(millis:Long?)
    fun getAlarm():Long?
    fun getParentErrorMessage():String
    fun entityChangedLive():LiveData<Boolean>
    fun timeChanged():Boolean
    fun checkPlace():Boolean
    fun updateAlarm(): Boolean
}

class TaskEntityHandler(private var loadedParent: TaskEntity): TaskEntityHandlerInterface {
    private var updatedParent: TaskEntity =loadedParent.copy()
    private var entityStatus= mutableMapOf(
        EntityPlaceholders.Name to false,
        EntityPlaceholders.Start to false,
        EntityPlaceholders.End to false,
        EntityPlaceholders.PriorityType to false,
        EntityPlaceholders.TaskType to false,
        EntityPlaceholders.Alarm to false
    )
    private val errorList= mutableMapOf(
        ErrorTypes.Name to validEntity,
        ErrorTypes.Time to validEntity
    )
    private var entityChanged=MutableLiveData<Boolean>()
    init {
        onResetEntity()
    }

    private fun onResetEntity(){
        resetStatus()
        resetErrorList()
        entityChanged.value=false
    }
    private fun resetStatus(){
        EntityPlaceholders.values().forEach {
            entityStatus[it]=false
        }
    }
    private fun isNewEntity():Boolean{
        if(loadedParent.task_name==""){return true}
        return false
    }
    private fun resetErrorList(){
        if(isNewEntity()){
            setAllWrong()
        }
        else{
            setAllCorrect()
        }
    }
    private fun setAllWrong(){
        ErrorTypes.values().forEach {
            errorList[it]=it.message
        }
    }
    private fun setAllCorrect(){
        ErrorTypes.values().forEach {
            errorList[it]= validEntity
        }
    }
    private fun checkIfAnyChanged():Boolean{
        return  entityStatus.any {
            it.value
        }
    }
    private fun updateEntityChanged(status:Boolean){
        if(status!=entityChanged.value){
            entityChanged.value=checkIfAnyChanged()
        }
    }
    private fun setPlaceholderStatus(placeholder: EntityPlaceholders, status:Boolean){
        entityStatus[placeholder] = status
    }
    private fun recordEntityChange(placeholder: EntityPlaceholders, status:Boolean){
        setPlaceholderStatus(placeholder, status)
        updateEntityChanged(status)
    }
    private fun validateName():String{
        if(RegexHelper.isNotBlank(updatedParent.task_name)){
            return validEntity
        }
        return ErrorTypes.Name.message
    }
    private fun validateTime():String{
        if(updatedParent.task_start<updatedParent.task_end){
            return validEntity
        }
        return ErrorTypes.Time.message
    }
    private fun validate(placeholder: EntityPlaceholders){
        when(placeholder){
            EntityPlaceholders.Name ->errorList[ErrorTypes.Name]=validateName()
            EntityPlaceholders.Start, EntityPlaceholders.End ->errorList[ErrorTypes.Time]=validateTime()
            else->return
        }
    }
    override fun resetManagedEntity(entity: TaskEntity) {
        loadedParent=entity
        updatedParent=loadedParent.copy()
        onResetEntity()
    }
    override fun getParentErrorMessage(): String {
        var message= validEntity
        ErrorTypes.values().forEach {
            if(errorList[it]!=""){
                message+="${errorList[it]}\n"
            }
        }
        return message
    }
    override fun getParent(): TaskEntity {
        return updatedParent
    }
    override fun setId(id: Long) {
        updatedParent.task_id=id
    }
    override fun getId(): Long {
        return updatedParent.task_id
    }
    override fun getName(): String {
        return updatedParent.task_name
    }
    override fun setName(name: String) {
        recordEntityChange(EntityPlaceholders.Name,name!=loadedParent.task_name)
        updatedParent.apply { task_name=name }
        validate(EntityPlaceholders.Name)
    }
    override fun getStart(): Long {
        return updatedParent.task_start
    }
    override fun setStart(date: Long) {
        recordEntityChange(EntityPlaceholders.Start,date!=loadedParent.task_start)
        updatedParent.apply { task_start=date }
        validate(EntityPlaceholders.Start)
    }
    override fun getEnd(): Long {
        return updatedParent.task_end
    }
    override fun setEnd(date: Long) {
        recordEntityChange(EntityPlaceholders.End,date!=loadedParent.task_end)
        updatedParent.apply { task_end=date }
        validate(EntityPlaceholders.End)
    }
    override fun getPriority():Int {
        return updatedParent.task_priority.ordinal
    }
    override fun setPriority(priority: PriorityTypes) {
        recordEntityChange(EntityPlaceholders.PriorityType,priority!=loadedParent.task_priority)
        updatedParent.apply { task_priority=priority }
        validate(EntityPlaceholders.PriorityType)
    }
    override fun getTaskType(): Int {
        return updatedParent.task_type.ordinal
    }
    override fun setTaskType(type: TaskTypes) {
        recordEntityChange(EntityPlaceholders.TaskType,type!=loadedParent.task_type)
        updatedParent.apply { task_type=type }
        validate(EntityPlaceholders.PriorityType)
    }

    override fun setAlarm(millis: Long?) {
        recordEntityChange(EntityPlaceholders.Alarm,millis!=loadedParent.task_alarm)
        updatedParent.apply { task_alarm=millis }
    }

    override fun getAlarm(): Long?{
        return updatedParent.task_alarm
    }

    override fun entityChangedLive(): LiveData<Boolean> {
        return entityChanged
    }

    override fun timeChanged(): Boolean {
        return entityStatus[EntityPlaceholders.End] == true || entityStatus[EntityPlaceholders.Start] == true
    }

    override fun checkPlace(): Boolean {
        return timeChanged()&&updatedParent.task_type== TaskTypes.Meeting
    }

   override fun updateAlarm(): Boolean {
       if(entityStatus[EntityPlaceholders.Alarm]==true) {return true}
       val hasAlarmTimeChanged=when(updatedParent.task_type){
           TaskTypes.Deadline-> entityStatus[EntityPlaceholders.End]==true
           TaskTypes.Meeting-> entityStatus[EntityPlaceholders.Start]==true
       }
       if(updatedParent.task_alarm!=null&&hasAlarmTimeChanged){return true}
       return false
    }

    companion object{
        enum class ErrorTypes(val message:String){
            Name(App.instance.getString(R.string.task_form_invalid_name_message)),
            Time(App.instance.getString(R.string.task_form_invalid_time_message))
        }
        enum class EntityPlaceholders{
            Name,Start,End,PriorityType,TaskType,Alarm
        }
        const val validEntity:String=""
    }
}