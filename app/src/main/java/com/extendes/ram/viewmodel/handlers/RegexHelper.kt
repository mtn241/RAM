package com.extendes.ram.viewmodel.handlers

object RegexHelper {
    private val phoneRegex=android.util.Patterns.PHONE.toRegex()
    private val emailRegex=android.util.Patterns.EMAIL_ADDRESS.toRegex()
    private val notBlankRegex=""".*\S+.*""".toRegex()
     fun isPhone(value:String):Boolean{
        if(value.matches(phoneRegex)){return true}
        return false
    }
    fun isEmail(value:String):Boolean{
        if(value.matches(emailRegex)){return true}
        return false
    }
    fun isNotBlank(value:String):Boolean{
        if(value.matches(notBlankRegex)){return true}
        return false
    }
}