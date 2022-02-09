package com.extendes.ram.viewmodel.handlers

interface TaskListInterface <T>{
    fun getList():MutableList<T>
    fun removeFromListAt(position: Int)
    fun insertToListAt(newItem: T):Int
    fun reloadList(newList:MutableList<T>)
    fun attachToId(id:Long)
    fun listChanged():Boolean
    fun getItemAt(position:Int): T
}