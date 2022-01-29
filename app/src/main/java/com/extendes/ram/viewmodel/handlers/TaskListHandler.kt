package com.extendes.ram.viewmodel.handlers

import com.extendes.ram.database.TaskTypes
import com.extendes.ram.database.TaskWithList


interface TaskListHandlerInterface{
    fun getList():MutableList<TaskWithList>
    fun setList(newList:MutableList<TaskWithList>)
    fun getPositionById(task: TaskWithList):Int
    fun insertTo(newItem: TaskWithList):Int
    fun getTaskAt(position: Int): TaskWithList
}


class TaskListHandler(private val currentList:MutableList<TaskWithList>): TaskListHandlerInterface {
    override fun getList(): MutableList<TaskWithList> {
        return currentList
    }

    override fun getPositionById(task: TaskWithList): Int {
        return currentList.indexOf(task)
    }

    override fun setList(newList: MutableList<TaskWithList>) {
        currentList.clear()
        currentList.addAll(newList)
    }

    override fun insertTo(newItem: TaskWithList): Int {
        val newPosition:Int=getPosition(newItem)
        currentList.add(newPosition,newItem)
        return newPosition
    }

    override fun getTaskAt(position: Int): TaskWithList {
        return currentList[position]
    }
    private fun getPosition(newItem: TaskWithList):Int{
        return when(newItem.parent.task_type){
            TaskTypes.Deadline->getDeadlinePosition(newItem)
            TaskTypes.Meeting->getMeetingPosition(newItem)
        }
    }
    private fun getMeetingPosition(newItem: TaskWithList):Int{
        val position:Int=currentList.indexOfFirst {
            it.parent.task_start>newItem.parent.task_end
        }
        return if(position>-1) position else currentList.size
    }
    private fun getDeadlinePosition(newItem: TaskWithList):Int{
        val position:Int=currentList.indexOfFirst {
            it.parent.task_end>newItem.parent.task_end
        }
        return if(position>-1) position else currentList.size
    }

}