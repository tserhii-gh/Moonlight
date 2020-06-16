package home.tuxhome.moonlight

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.loading_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    //    val config_keys = arrayOf("sync_startup", "sync_delay", "switch_delay","save_delay")
    val default_config = mapOf(
        "sync_startup" to 3500,
        "sync_delay" to 12500,
        "switch_delay" to 1000,
        "save_delay" to 12500
    )

    lateinit var config: Map<String, Int>
    val SYNC_STARTUP_CONST = 3500
    val SYNC_DELAY_CONST = 12500
    val SWITCH_DELAY_CONST = 1000
    val SAVE_DELAY_CONST = 12500
    var SYNC_STARTUP = 0
    var SYNC_DELAY = 0
    var SWITCH_DELAY = 0
    var SAVE_DELAY = 0
    var APP_SETTINGS = "settings"
    var RELAY_STATE = false
    var READY = false
    lateinit var pref: SharedPreferences
    lateinit var handler: Handler

    private var allControls = ArrayList<Button>()

    //    private var allProgs = ArrayList<RadioButton>()
    val TAG = "Moonlight"
    val REALY_ON_URL = "http://192.168.4.1/on"
    val REALY_OFF_URL = "http://192.168.4.1/off"
    val REALY_STATUS_URL = "http://192.168.4.1/status"

    val PROGS = arrayOf(
        "sun_white",
        "red",
        "green",
        "blue",
        "green_blue",
        "red_green",
        "blue_red",
        "evening_sea",
        "evening_river",
        "riviera",
        "neutral_white",
        "rainbow",
        "river_of_colors",
        "disco",
        "four_seasons",
        "party"
    )
    val PROG_INFO = arrayOf(
        "SUN WHITE (warm white)",
        "RED (fixed color red)",
        "GREEN (fixed color green)",
        "BLUE (fixed color blue)",
        "GREEN-BLUE (fixed color green-blue)",
        "RED-GREEN (fixed color red-green)",
        "BLUE-RED (fixed color blue-red)",
        "EVENING SEA (slow animation red-blue)",
        "EVENING RIVER (slow animation red-green)",
        "RIVIERA (slow animation green-blue)",
        "NEUTRAL WHITE (cold white)",
        "RAINBOW (slow animation blue-red-green)",
        "RIVER OF COLORS (rainbow followed by four seasons)",
        "DISCO (fast animation)",
        "FOUR SEASONS (slow animation red-blue-green-violet)",
        "PARTY (fast animation)"
    )

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rc = RemoteControl()
        pref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
//        reloadTimerValues()
        config = loadConfigValues()
//        Utils().getAllControls(controls, allControls)
//        deactivateControls()
        initControls()
//        remoteRelaySTATUS()
//        getAllProgs(progs_rb_group)


        handler = Handler()
//        initControls()
//        button_off.isEnabled = false
//        button_next.isEnabled = false
//        button_sync.isEnabled = false
//        button_save.isEnabled = false


        button_on.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main){
                if (rc.relayOnCoroutine()){
                    toggleControls()
                    toggleAllProgs()
                } else Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
            }
//            val toast = Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT)
//            Thread {
//                if (rc.relay_on()){
//                    enableAllProgs(progs_rb_group)
//                    button_on.isEnabled = false
//                    button_off.isEnabled = true
//                    button_next.isEnabled = true
//                    button_sync.isEnabled = true
//                    button_save.isEnabled = true
//                } else
//                {
//                    toast.show()
//                }
////                Log.e(TAG, rc.relay_status().toString())
//            }.start()

        }

        button_off.setOnClickListener {
//            remoteRelayOFF()
            Thread {
                rc.relay_off()
//                Log.e(TAG, rc.relay_status().toString())
            }.start()

            toggleControls()
            toggleAllProgs()
        }

        button_save.setOnClickListener {
            allControls.forEach {
                it.isEnabled = false
            }
            RELAY_STATE = false
//            Log.e(TAG, allProgs.size.toString())
//            disableAllProgs(progs_rb_group)
            toggleAllProgs()
            val dlg = loadingDialog("Saving selected PROGRAM")
            dlg.show()
            handler.postDelayed({
                Log.e(TAG, "Thread")
                dlg.dismiss()
                RELAY_STATE = true
                button_on.isEnabled = false
                button_off.isEnabled = true
                button_next.isEnabled = true
                button_sync.isEnabled = true
                button_save.isEnabled = true
                enableAllProgs(progs_rb_group)
//                allButtons.forEach {
//                    it.isEnabled = true
//                }
            }, config.getValue("save_delay").toLong())


        }

        button_next.setOnClickListener {
            allControls.forEach {
                it.isEnabled = false
            }
            RELAY_STATE = false
            Thread { rc.relay_off() }.start()
            handler.postDelayed({
                Thread { rc.relay_on() }.start()

                Log.e(TAG, "Thread")
                switchProg(progs_rb_group)
                RELAY_STATE = true
                button_on.isEnabled = false
                button_off.isEnabled = true
                button_next.isEnabled = true
                button_sync.isEnabled = true
                button_save.isEnabled = true
                enableAllProgs(progs_rb_group)
//                allButtons.forEach {
//                    it.isEnabled = true
//                }
            }, config.getValue("switch_delay").toLong())
        }

        if (progs_rb_group != null) {
            PROGS.forEach {
                progs_rb_group.addView(
                    RadioButton(this).apply {
                        text = PROG_INFO[PROGS.indexOf(it)]
                        tag = it
                    }
                )
            }
            progs_rb_group.addView(TextView(this).apply {
            })
            progs_rb_group.setOnCheckedChangeListener { group, checkedId ->
                Toast.makeText(applicationContext, checkedId.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        progs_rb_group.check(11)
//        disableAllProgs(progs_rb_group)
        toggleAllProgs()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        val ready = menu?.findItem(R.id.ready)
        if (READY) ready?.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.settigs_action) {

            Log.e(TAG, READY.toString())
            SettingsDialog().show(supportFragmentManager, "SettingsDialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadConfigValues(): MutableMap<String, Int> {
        return default_config.mapValues { pref.getInt(it.key, default_config.getValue(it.key)) }.toMutableMap()
    }


//    private fun reloadTimerValues() {
////        if (pref.contains(APP_SETTINGS)){
//        val config_keys = arrayOf("sync_startup", "sync_delay", "switch_delay", "save_delay")
//        SYNC_STARTUP = pref.getInt("sync_startup", SYNC_STARTUP_CONST)
//        SYNC_DELAY = pref.getInt("sync_delay", SYNC_DELAY_CONST)
//        SWITCH_DELAY = pref.getInt("switch_delay", SWITCH_DELAY_CONST)
//        SAVE_DELAY = pref.getInt("save_delay", SAVE_DELAY_CONST)
//    }

    private fun saveTimerValues(m: Map<String, Int>){
        val editor = pref.edit()
        default_config.keys.forEach {
            editor.putInt(it, m.getValue(it))
        }
        editor.apply()
        config = m
    }

    fun toggleControls(){
        val parent = controls // radio buttons group
        for (index in 0 until parent.childCount)
            parent.getChildAt(index).isEnabled = !parent.getChildAt(index).isEnabled
    }

    fun disableControls(){
        val parent = controls // radio buttons group
        for (index in 0 until parent.childCount)
            parent.getChildAt(index).isEnabled = false
    }

    private fun toggleAllProgs() {
        val parent = progs_rb_group // radio buttons group
        for (index in 0 until parent.childCount)
            parent.getChildAt(index).isEnabled = !parent.getChildAt(index).isEnabled
    }


    private fun disableAllProgs(parent: ViewGroup) {
        for (index in 0 until parent.childCount)
            parent.getChildAt(index).isEnabled = false
    }

    private fun enableAllProgs(parent: ViewGroup) {
        for (index in 0 until parent.childCount)
            parent.getChildAt(index).isEnabled = true
    }

    @SuppressLint("ResourceType")
    private fun switchProg(v: ViewGroup) {
        val checked = v.progs_rb_group.checkedRadioButtonId
        if (checked > PROGS.size - 1) {
            v.progs_rb_group.check(1)
        } else {
            v.progs_rb_group.check(checked + 1)
        }
    }

    private fun loadingDialog(msg: String): AlertDialog {
//        val load = ProgressDialog(this)
        val builder = AlertDialog.Builder(this)
        val v = layoutInflater.inflate(R.layout.loading_layout, null)
        builder.setView(v)
        v.loading_title.text = msg
//        builder.show()

        return builder.create()
    }

    fun resetConfig() {
        saveTimerValues(default_config)
    }

    fun saveConfig(config: Map<String, Int>) {
        saveTimerValues(config)

    }

//    fun activateControls(){
////        enableAllProgs(progs_rb_group)
//        toggleAllProgs()
//        button_on.isEnabled = false
//        button_off.isEnabled = true
//        button_next.isEnabled = true
//        button_sync.isEnabled = true
//        button_save.isEnabled = true
//    }

    fun initControls(){
        val parent = controls // radio buttons group
        for (index in 1 until parent.childCount)
            parent.getChildAt(index).isEnabled = false
    }
//
//    fun deactivateControls(){
//        toggleAllProgs()
//        button_on.isEnabled = true
//        button_off.isEnabled = false
//        button_next.isEnabled = false
//        button_sync.isEnabled = false
//        button_save.isEnabled = false
//    }
}