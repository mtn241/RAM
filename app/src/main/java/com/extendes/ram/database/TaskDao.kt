package com.extendes.ram.database

import androidx.room.*


@Dao
interface TaskDao {
    @Insert
    suspend fun insert(item: TaskEntity):Long

    @Query("SELECT * FROM task_table WHERE task_id=:id")
    suspend fun getTaskById(id: Long): TaskWithList

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: TaskEntity)

    @Delete
    suspend fun delete(item: TaskEntity)

    @Query("DELETE FROM task_table")
    fun deleteAll()

    @Query("DELETE FROM task_table WHERE task_end<:start")
    fun deleteAllEntitiesTill(start:Long)

    @Query("SELECT task_id FROM task_table WHERE task_end<:start")
    fun getAllEntitiesTill(start:Long):List<Long>

    @Transaction
    @Query("SELECT * FROM task_table WHERE task_type='Deadline' AND task_start<:date_end AND task_end>:date_start ORDER BY task_end ASC")
    suspend fun getAllDeadlinesForDate(date_start:Long,date_end:Long):List<TaskWithList>

    @Transaction
    @Query("SELECT * FROM task_table WHERE task_type='Meeting' AND task_start<:date_end AND task_end>:date_start ORDER BY task_start ASC")
    suspend fun getAllMeetingsForDate(date_start:Long,date_end:Long):List<TaskWithList>

    @Transaction
    @Query("SELECT * FROM task_table WHERE task_type='Meeting' AND ((task_start>=:date_start AND task_start<=:date_end) OR (task_end>=:date_start AND task_end<=:date_end) OR (task_start<:date_start AND task_end>:date_end ))")
    suspend fun checkPlace(date_start:Long,date_end:Long):List<TaskEntity>

    @Transaction
    @Query("SELECT * FROM task_table WHERE task_type='Meeting' AND task_id IS NOT :id AND ((task_start>=:date_start AND task_start<=:date_end) OR (task_end>=:date_start AND task_end<=:date_end) OR (task_start<:date_start AND task_end>:date_end ))")
    suspend fun checkPlaceUpdate(date_start:Long,date_end:Long,id:Long):List<TaskEntity>
}

@Dao
interface ListDao{

    @Delete
    suspend fun deleteItem(item: ListItem)

    @Insert
    suspend fun insertList(list: List<ListItem>)

    @Insert
    suspend fun insertItem(item: ListItem)

    @Query("DELETE  FROM listitem WHERE parent_id=:parent")
    suspend fun deleteAllTaskItems(parent:Long)
}

@Dao
interface ContactListDao{

    @Delete
    suspend fun deleteItem(item: ContactListItem)

    @Insert
    suspend fun insertList(contact_list: List<ContactListItem>)

    @Insert
    suspend fun insertItem(item: ContactListItem)

    @Query("DELETE  FROM listitem WHERE parent_id=:parent")
    suspend fun deleteAllTaskItems(parent:Long)
}