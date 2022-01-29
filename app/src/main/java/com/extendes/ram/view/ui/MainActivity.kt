package com.extendes.ram.view.ui


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.extendes.ram.*
import com.extendes.ram.viewmodel.handlers.AlertReceiver
import com.extendes.ram.viewmodel.handlers.DateFormatHandler
import com.extendes.ram.viewmodel.model.AlarmData
import com.extendes.ram.viewmodel.model.MainViewModel
import kotlinx.android.synthetic.main.form_back_dialog.*


class MainActivity : AppCompatActivity() {


    private val formFragment= TaskFormFragment()

    private val listFragment= MainListFragment()

    private val viewModel: MainViewModel by viewModels()

    private lateinit var formBackView: View

    private lateinit var  formBackDialog: androidx.appcompat.app.AlertDialog

    private lateinit var toSettingsLauncher: ActivityResultLauncher<Intent>

    private lateinit var preferencesListener:SharedPreferences.OnSharedPreferenceChangeListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustFontScale( resources.configuration)
        viewModel.cleanOutdatedTasks()
        setContentView(R.layout.activity_main)
        formBackView= View.inflate(this, R.layout.form_back_dialog, null)
        formBackDialog = androidx.appcompat.app.AlertDialog.Builder(this).create()
        formBackDialog.setView(formBackView)
        toSettingsLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}
        setFragment (list_fragment)
        runIfWakeOnTask(intent)
        val preferences= PreferenceManager.getDefaultSharedPreferences(this)
        preferences.getString("settings_date_format", DateFormatHandler.defaultDateFormat)?.let { viewModel.setDateFormat(it) }
        preferences.getString("settings_time_format", DateFormatHandler.defaultTimeFormat)?.let { viewModel.setTimeFormat(it) }
        preferencesListener= SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when(key){
                "settings_date_format"->{
                    sharedPreferences.getString(key,"")?.let {
                        viewModel.setDateFormat(it)
                    }
                }
                "settings_time_format"->{
                    sharedPreferences.getString(key,"")?.let {
                        viewModel.setTimeFormat(it)
                    }
                }
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)

        val toFormObserver=Observer<Boolean>{
            if(it){
                if(!lastIs(form_fragment)){
                    setFragment (form_fragment)
                }
            }

            else{
                if(lastIs(form_fragment)){
                    supportFragmentManager.popBackStack()

                }
            }
        }
        viewModel.toFormLive().observe(this,toFormObserver)




        val alarmObserver= Observer<AlarmData> {
            val alarm=this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent=Intent(this, AlertReceiver::class.java)
            println("ID is"+it.id)
            intent.apply {
                action= App.ACTION_ON_TASK_ALARM
                data= Uri.parse(it.id)
            }
            intent.putExtra("message",it.message)
            intent.putExtra("color",ResourcesCompat.getColor(this.resources,it.color,null))
            val pending= PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT)
            if(it.cancel){alarm.cancel(pending)}
            else{ alarm.setExact(AlarmManager.RTC,it.alarm,pending)}
        }
        viewModel.getLiveAlarmData().observe(this,alarmObserver)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        runIfWakeOnTask(intent)
    }



    override fun onBackPressed() {
        if(lastIs(form_fragment)){
            viewModel.onFormListChanged()
            if(viewModel.getFormTaskChanged().value==true){
                onFormBackDialog()
            }
            else{
                supportFragmentManager.popBackStack()
            }
        }
        else{
            /*
            if(!lastIs(list_fragment)){

            }

             */
            super.onBackPressed()
            this.finish()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
              R.id.top_menu_add ->{
                  viewModel.setNewAsManaged()
                  if(!lastIs(form_fragment)){
                      setFragment ( form_fragment)
                  }

              }
              R.id.top_menu_settings ->{
                  val settingsIntent= Intent(this, SettingsActivity()::class.java)
                  toSettingsLauncher.launch(settingsIntent)
              }
        }
        return true
    }
    private fun onFormBackDialog(){
        formBackDialog.show()
        formBackDialog.form_discard_yes.setOnClickListener {
            viewModel.returnFromForm()
            formBackDialog.dismiss()
        }
        formBackDialog.form_discard_no.setOnClickListener {
            formBackDialog.dismiss()
        }
    }
    private fun setFragment ( fragName:String){
        when(fragName){
            list_fragment ->{
                supportFragmentManager.beginTransaction().apply {
                add(R.id.main_fragment,listFragment)
                addToBackStack(fragName)
                commit() }
            }
            form_fragment ->{
                supportFragmentManager.beginTransaction().apply {
                    add(R.id.main_fragment,formFragment)
                    addToBackStack(fragName)
                    commit() }
            }
        }
    }
    private fun lastIs(fragName:String):Boolean{
        val count = supportFragmentManager.backStackEntryCount
        if (count<=0) return false
        val lastEntry=supportFragmentManager.getBackStackEntryAt(count-1)
        if(lastEntry.name!=fragName){
            return false
        }
        return true
    }
    private fun runIfWakeOnTask(intent:Intent?){
        if(intent?.action== App.ACTION_NOTIFICATION_CLICK){
            val id:Long=intent.getLongExtra("task_id",0)
            viewModel.onWakeOnTaskNotification(id)
        }
    }
    private fun adjustFontScale(configuration: Configuration) {
        if (configuration.fontScale > 1.3) {
            configuration.fontScale = 1.30f
            val metrics = resources.displayMetrics
            this.windowManager.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density
            resources.updateConfiguration(configuration, metrics)
        }
    }
    companion object{
        const val form_fragment:String="FormFragment"
        const val list_fragment:String="ListFragment"
    }
}