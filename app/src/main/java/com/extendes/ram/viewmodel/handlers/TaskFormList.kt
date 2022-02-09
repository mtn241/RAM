package com.extendes.ram.viewmodel.handlers


import com.extendes.ram.database.ListItem


interface ResolveItem{
    fun packToListItem(value:String,parentId:Long): ListItem?
}
abstract class TaskFormListInterface: ResolveItem,TaskListInterface<ListItem>
class TaskFormList (private var loadedList:MutableList<ListItem>): TaskFormListInterface() {
    private var isEmptyOnLoad:Boolean=loadedList.isEmpty()
    private var onListChanged:Boolean=false
    init {
        resetToDefault()
    }
    private fun resetToDefault(){
        isEmptyOnLoad=loadedList.isEmpty()
        onListChanged=false
    }
    private fun updateOnListChanged(){
        onListChanged = isEmptyOnLoad!=loadedList.isEmpty()
    }
    private fun validate(value:String):Boolean{
        return RegexHelper.isNotBlank(value)
    }
    override fun reloadList(newList: MutableList<ListItem>) {
        loadedList=newList
        resetToDefault()
    }

    override fun getList(): MutableList<ListItem> {
        return loadedList
    }
    override fun packToListItem(value: String,parentId:Long): ListItem? {
        if(validate(value)) {
            return ListItem(0,parentId,value)
        }
        return null
    }
    override fun removeFromListAt(position: Int) {
        loadedList.removeAt(position)
        updateOnListChanged()
    }
    override fun insertToListAt(newItem: ListItem): Int {
        loadedList.add(newItem)
        updateOnListChanged()
        return loadedList.size-1
    }
    override fun attachToId(id: Long) {
        loadedList.forEach {
            it.apply {
                parent_id=id
            }
        }
    }
    override fun getItemAt(position: Int): ListItem {
        return loadedList[position]
    }

    override fun listChanged(): Boolean {
        return onListChanged
    }
}