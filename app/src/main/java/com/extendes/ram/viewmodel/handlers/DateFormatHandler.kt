package com.extendes.ram.viewmodel.handlers


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*


interface DateFormatHandlerInterface{
    fun dateToString(input: Date):String
    fun timeToString(input: Date):String
    fun setDatePattern(pattern:String)
    fun setTimePattern(pattern:String)
    fun getLiveDateFormat():LiveData<SimpleDateFormat>
    fun getLiveTimeFormat():LiveData<SimpleDateFormat>
    fun display24Hour():LiveData<Boolean>

}

class  DateFormatHandler : DateFormatHandlerInterface {
    private val dateFormat=MutableLiveData(SimpleDateFormat(defaultDateFormat))
    private val timeFormat=MutableLiveData(SimpleDateFormat(defaultTimeFormat))
    private val show24Hour=MutableLiveData(resolve24Hour(defaultTimeFormat))


    override fun dateToString(input: Date): String {
        return dateFormat.value!!.format(input)
    }

    override fun timeToString(input: Date): String {
        return timeFormat.value!!.format(input)
    }

    override fun setDatePattern(pattern:String) {
        if(isValidatePattern(pattern)){
            dateFormat.value= SimpleDateFormat(pattern)
        }

    }
    override fun setTimePattern(pattern:String) {
        if(isValidatePattern(pattern)){
           show24Hour.value= resolve24Hour(pattern)
           timeFormat.value= SimpleDateFormat(pattern)
        }
    }
    private fun resolve24Hour(pattern: String):Boolean{
        return pattern.last()!='a'
    }

    override fun getLiveDateFormat(): LiveData<SimpleDateFormat> = dateFormat

    override fun getLiveTimeFormat(): LiveData<SimpleDateFormat> = timeFormat

    override fun display24Hour(): LiveData<Boolean> = show24Hour



    private fun isValidatePattern(pattern:String):Boolean{
        return true //TODO dummy reimplement
    }

    companion object{
        const val defaultDateFormat:String="dd/MM/yy"
        const val defaultTimeFormat:String="HH:mm"
    }
}