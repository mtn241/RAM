package com.extendes.ram.viewmodel.handlers

import com.extendes.ram.database.ContactListItem
import com.extendes.ram.database.ContactTypes




abstract class TaskFormContactListInterface:TaskListInterface<ContactListItem>{
    abstract fun createContactItem(parentId:Long, value: String, contactType: ContactTypes):ContactListItem?
}
class TaskFormContactList(private var loadedList:MutableList<ContactListItem>):
    TaskFormContactListInterface(){
    private var isEmptyOnLoad:Boolean=loadedList.isEmpty()
    private var onContactListChanged:Boolean=false
    init {
        resetToDefault()
    }
    private fun resetToDefault(){
        isEmptyOnLoad=loadedList.isEmpty()
        onContactListChanged=false
    }
    private fun updateOnListChanged(){
        onContactListChanged = isEmptyOnLoad!=loadedList.isEmpty()
    }

    private fun validateContact(value: String, contactType: ContactTypes): Boolean {
        return when(contactType){
            ContactTypes.PHONE->RegexHelper.isPhone(value)
            ContactTypes.MAIL->RegexHelper.isEmail(value)
            ContactTypes.OTHER->RegexHelper.isNotBlank(value)
        }
    }

    override fun reloadList(newList: MutableList<ContactListItem>) {
        loadedList=newList
        resetToDefault()
    }

    override fun getList(): MutableList<ContactListItem> {
        return loadedList
    }

    override fun removeFromListAt(position: Int) {
        loadedList.removeAt(position)
        updateOnListChanged()
    }

    override fun insertToListAt(contact: ContactListItem): Int {
        loadedList.add(contact)
        updateOnListChanged()
        return loadedList.size-1
    }

    override fun createContactItem(parentId:Long, value: String, contactType: ContactTypes): ContactListItem? {
        return if(validateContact(value,contactType)){
            return ContactListItem(0,parentId,value,contactType)
        } else{
            null
        }
    }

    override fun listChanged(): Boolean {
        return onContactListChanged
    }

    override fun attachToId(id: Long) {
        loadedList.forEach {
            it.apply {
                parent_id=id
            }
        }
    }
    override fun getItemAt(position: Int): ContactListItem {
        return loadedList[position]
    }
}