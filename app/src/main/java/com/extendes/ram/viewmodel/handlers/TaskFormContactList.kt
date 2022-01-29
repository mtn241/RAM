package com.extendes.ram.viewmodel.handlers

import com.extendes.ram.database.ContactListItem
import com.extendes.ram.database.ContactTypes


interface TaskFormContactListInterface{
    fun reloadContactList (newList: MutableList<ContactListItem>)
    fun getContactList():MutableList<ContactListItem>
    fun removeFromContactListAt(position: Int)
    fun insertToContactListAt(contact: ContactListItem):Int
    fun createContactItem(parentId:Long,value: String, contactType: ContactTypes): ContactListItem?
    fun attachToId(id:Long)
    fun contactListChanged():Boolean
}

class TaskFormContactList(private var loadedList:MutableList<ContactListItem>):
    TaskFormContactListInterface {
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

    override fun reloadContactList(newList: MutableList<ContactListItem>) {
        loadedList=newList
        resetToDefault()
    }

    override fun getContactList(): MutableList<ContactListItem> {
        return loadedList
    }

    override fun removeFromContactListAt(position: Int) {
        loadedList.removeAt(position)
        updateOnListChanged()
    }

    override fun insertToContactListAt(contact: ContactListItem): Int {
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

    override fun contactListChanged(): Boolean {
        return onContactListChanged
    }

    override fun attachToId(id: Long) {
        loadedList.forEach {
            it.apply {
                parent_id=id
            }
        }
    }
}