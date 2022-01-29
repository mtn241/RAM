package com.extendes.ram.viewmodel.handlers

import com.extendes.ram.database.*
import com.extendes.ram.App


class TaskRepository {
    private val taskDao: TaskDao
    private val listDao: ListDao
    private val contactListDao: ContactListDao

    init {
        val database= TaskDatabase.getTaskDataBase(App.instance)
        taskDao=database.taskDao()
        listDao=database.listDao()
        contactListDao=database.contactListDao()
    }
    suspend fun getTaskById(id:Long): TaskWithList {
        return taskDao.getTaskById(id)
    }
    suspend fun insert(taskEntity: TaskEntity):Long{
        return taskDao.insert(taskEntity)
    }
    suspend fun update(taskEntity: TaskEntity){
        return taskDao.update(taskEntity)
    }
    suspend fun delete(taskEntity: TaskEntity){
        taskDao.delete(taskEntity)
    }
    /* TODO not used yet
    suspend fun deleteAll(){
        taskDao.deleteAll()
    }
    */
    suspend fun getAllDeadlines(date_start:Long,date_end:Long):List<TaskWithList>{
        return taskDao.getAllDeadlinesForDate(date_start,date_end)
    }
    suspend fun getAllMeetings(date_start:Long,date_end:Long):List<TaskWithList>{
        return taskDao.getAllMeetingsForDate(date_start,date_end)
    }
    suspend fun clearTaskList(parent:Long){
        listDao.deleteAllTaskItems(parent)
    }
    suspend fun clearTaskContactList(parent:Long){
        contactListDao.deleteAllTaskItems(parent)
    }
    suspend fun checkPlaceForMeeting(date_start:Long,date_end:Long):List<TaskEntity>{
        return taskDao.checkPlace(date_start,date_end)
    }
    suspend fun checkPlaceForMeetingUpdate(date_start:Long,date_end:Long,id:Long):List<TaskEntity>{
        return taskDao.checkPlaceUpdate(date_start,date_end,id)
    }
    suspend fun insertTaskList(list: List<ListItem>){
        listDao.insertList(list)
    }

    suspend fun insertTaskListItem(item: ListItem){
        listDao.insertItem(item)
    }

    suspend fun deleteListItem(item: ListItem){
        listDao.deleteItem(item)
    }




    suspend fun insertTaskContactList(list: List<ContactListItem>){
        contactListDao.insertList(list)
    }


    suspend fun insertTaskContactListItem(item: ContactListItem){
        contactListDao.insertItem(item)
    }

    suspend fun deleteContactItem(item: ContactListItem){
        contactListDao.deleteItem(item)
    }

    suspend fun deleteOutDated(start:Long){
        val list=taskDao.getAllEntitiesTill(start)
        list.forEach {
            contactListDao.deleteAllTaskItems(it)
            listDao.deleteAllTaskItems(it)
        }
        taskDao.deleteAllEntitiesTill(start)
    }
}