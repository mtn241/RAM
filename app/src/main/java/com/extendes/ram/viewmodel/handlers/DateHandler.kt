package com.extendes.ram.viewmodel.handlers


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*



interface DateHandlerInterface{
    fun getDateStart():Long
    fun getDateEnd():Long
    fun trimSeconds(date: Date): Date
    fun dayUp()
    fun dayDown()
    fun setDate(day:Int,month:Int,year:Int)
    fun setDateOnWake(dateAsLong:Long)
    fun today()
    fun getCalendar():Calendar
    fun getLiveDate():LiveData<Date>
    fun longToDate(input:Long):Date
    fun dateToLong(input:Date):Long
    fun inSameDay(dateOne:Long,dateTwo:Long):Boolean

}

class DateHandler : DateHandlerInterface {
    private val calendar=Calendar.getInstance()
    private val date=MutableLiveData<Date>()
    init {
        date.value=calendar.time
    }

    override fun inSameDay(dateOne: Long, dateTwo: Long) :Boolean{
        if(compareFormat.format(longToDate(dateTwo))== compareFormat.format(longToDate(dateOne))){
            return true
        }
        return false
    }

    override fun setDateOnWake(dateAsLong: Long) {
        val dateToSet=longToDate(dateAsLong)
        calendar.time=dateToSet
        if(dayChanged()){
            date.value=calendar.time
        }
    }

    override fun trimSeconds(date: Date): Date {
        val normalized=Calendar.getInstance()
        normalized.time=date
        normalized.timeInMillis-=normalized.timeInMillis%60000
        return normalized.time
    }

    override fun longToDate(input: Long): Date {
        val calendar =Calendar.getInstance()
        calendar.timeInMillis=input
        return calendar.time
    }

    override fun dateToLong(input: Date): Long {
        val calendar =Calendar.getInstance()
        calendar.time=input
        return calendar.timeInMillis
    }

    override fun getDateStart(): Long {
        val start=Calendar.getInstance()
        start.time=date.value
        start.set(Calendar.HOUR_OF_DAY,0)
        start.set(Calendar.MINUTE,0)
        return start.timeInMillis
    }


    override fun getDateEnd(): Long {
        val end=Calendar.getInstance()
        end.time=date.value
        end.set(Calendar.HOUR_OF_DAY,24)
        end.set(Calendar.MINUTE,0)
        return end.timeInMillis
    }

    override fun dayUp() {
        calendar.add(Calendar.DATE,1)
        date.value=calendar.time
    }
    override fun dayDown() {
        calendar.add(Calendar.DATE,-1)
        date.value=calendar.time
    }
    override fun setDate(day:Int,month:Int,year:Int) {
        calendar.set(year,month,day)
        if(dayChanged()){
            date.value=calendar.time
        }
    }
    override fun today() {
        calendar.time=Date()
        if(dayChanged()){
            date.value=calendar.time
        }
    }

    override fun getLiveDate(): LiveData<Date> = date


    override fun getCalendar(): Calendar {
        return this.calendar
    }

    private fun dayChanged():Boolean{
        val liveDate=Calendar.getInstance()
        liveDate.time=date.value
        if(compareFormat.format(liveDate.time)!= compareFormat.format(calendar.time)){
            return true
        }
        return false
    }
    companion object{
        private const val comparePattern="yyyy/MM/dd"
        val compareFormat=SimpleDateFormat(comparePattern)
    }
}