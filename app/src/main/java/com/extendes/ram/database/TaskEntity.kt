package com.extendes.ram.database

import androidx.room.*
import com.extendes.ram.R

enum class PriorityTypes(val background:Int,val color:Int,val entityName:Int){
    Lowest(R.drawable.low_bordered, R.color.priority_low, R.string.task_form_priority_low),
    Medium(R.drawable.medium_bordered, R.color.priority_medium, R.string.task_form_priority_medium),
    Urgent(R.drawable.urgent_bordered, R.color.priority_urgent, R.string.task_form_priority_urgent)
}
enum class TaskTypes(val entityName:Int,val alarm:Int){
    Deadline(R.string.task_form_type_deadline, R.string.alarm_deadline),
    Meeting(R.string.task_form_type_meeting, R.string.alarm_meeting)
}
enum class ContactTypes(val icon:Int,val entityName:Int){
    PHONE(R.drawable.ic_task_contact_list_phone, R.string.task_form_contact_phone),
    MAIL(R.drawable.ic_task_contact_list_mail, R.string.task_form_contact_email),
    OTHER(R.drawable.ic_task_contact_list_other, R.string.task_form_contact_other)
}


@Entity(tableName = "task_table")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) var task_id:Long=0,
    var task_name:String,
    var task_start:Long,
    var task_end:Long,
    var task_type: TaskTypes,
    var task_priority: PriorityTypes,
    var task_alarm:Long?
)

@Entity
data class ListItem(
    @PrimaryKey(autoGenerate = true) var id:Long,
    var parent_id:Long,
    var list_item:String
)



@Entity
data class ContactListItem(
    @PrimaryKey(autoGenerate = true) var id:Long,
    var parent_id:Long,
    var contact_item:String,
    var contact_type: ContactTypes
)


data class TaskWithList (
    @Embedded var parent: TaskEntity,
    @Relation(parentColumn = "task_id",entityColumn = "parent_id")
    var contact_list:MutableList<ContactListItem>,
    @Relation(parentColumn = "task_id",entityColumn = "parent_id")
    var list:MutableList<ListItem>
)




