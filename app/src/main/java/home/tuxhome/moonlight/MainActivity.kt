package home.tuxhome.moonlight

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
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

    val defaultConfig = mapOf(
        "sync_startup" to 2000,
        "sync_delay" to 12200,
        "switch_delay" to 750,
        "save_delay" to 12500
    )

    lateinit var config: Map<String, Int>
    var APP_SETTINGS = "settings"
    var READY = false
    lateinit var pref: SharedPreferences
    lateinit var utils: Utils
    val TAG = "Moonlight"
    lateinit var rc: RemoteControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
        rc = RemoteControl()
        config = loadConfigValues() // load configuration
        utils =
            Utils(pref, controls, progs_rb_group, config, defaultConfig, RGBPrograms.NEUTRAL_WHITE)
        utils.initControls() // init control buttons
//        utils.loadLastProgram() // load last selected program

        button_on.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                if (rc.relayOn()) {
                    utils.toggleControls()
                    utils.toggleAllProgs()
                } else Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        button_off.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                if (rc.relayOff()) {
                    utils.toggleControls()
                    utils.toggleAllProgs()
                } else Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        button_next.setOnClickListener {
            utils.disableControls()
            GlobalScope.launch(Dispatchers.Main) {
                if (!rc.relayOff()) Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
                    .show()
            }
            GlobalScope.launch(Dispatchers.Main) {
                Log.e(TAG, config.toString())
                if (rc.relayOn(delay_time = config.getValue("switch_delay").toLong())) {
                    switchProg(progs_rb_group)
                    resumeControls()
                } else Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        button_sync.setOnClickListener {
            val dlg = LoadingDialog("Syncing LED Lights...")
            dlg.show(supportFragmentManager, "loading_dialog")
            dlg.isCancelable = false
            GlobalScope.launch(Dispatchers.Main) {
                rc.relayOff() // off lights for 15 seconds
                rc.relayOn(delay_time = 15000) // on for sync startup
                rc.relayOff(delay_time = config.getValue("sync_startup").toLong())
                rc.relayOn(
                    delay_time = config.getValue("sync_delay").toLong()
                ) // on lights for 12 seconds
                rc.relayOff(delay_time = config.getValue("switch_delay").toLong()) // off lights
                rc.relayOn(delay_time = config.getValue("switch_delay").toLong()) // off lights
                rc.relayOff(delay_time = config.getValue("switch_delay").toLong()) // off lights
                rc.relayOn(delay_time = config.getValue("switch_delay").toLong()) // off lights
                dlg.dismiss()
                progs_rb_group.check(progs_rb_group.findViewWithTag<RadioButton>(RGBPrograms.EVENING_SEA.program_id).id)
            }

        }

        button_save.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                if (!rc.relayOff()) Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT)
                    .show()
            }
            val dlg = LoadingDialog("Saving selected program...")
            dlg.show(supportFragmentManager, "loading_dialog")
            dlg.isCancelable = false
            GlobalScope.launch(Dispatchers.Main) {
                if (rc.relayOn(delay_time = config.getValue("save_delay").toLong())) {
                    dlg.dismiss()
                } else Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
        }
        generatePrograms() // generate available rgb programs
        utils.toggleAllProgs() // toggle radio button group (startup state disabled!)
    }


    private fun generatePrograms() {
        if (progs_rb_group != null) {
            RGBPrograms.values().forEach {
                progs_rb_group.addView(
                    RadioButton(this).apply {
                        text = it.program_info
                        tag = it.program_id
                    }
                )
            }

            progs_rb_group.addView(TextView(this).apply {
            })
//            progs_rb_group.setOnCheckedChangeListener { group, checkedId ->
//                Toast.makeText(applicationContext, checkedId.toString(), Toast.LENGTH_SHORT).show()
//            }

            // TODO load value from SharedPreferences
            progs_rb_group.check(progs_rb_group.findViewWithTag<RadioButton>(utils.loadLastProgram()).id)

            progs_rb_group.setOnCheckedChangeListener { group, checkedId ->
                utils.saveProgram(group.findViewById<RadioButton>(checkedId).tag.toString())
            }
        }
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
            SettingsDialog().show(supportFragmentManager, "SettingsDialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun loadConfigValues(): MutableMap<String, Int> {
        return defaultConfig.mapValues { pref.getInt(it.key, defaultConfig.getValue(it.key)) }
            .toMutableMap()
    }

    private fun saveTimerValues(m: Map<String, Int>) {
        val editor = pref.edit()
        defaultConfig.keys.forEach {
            editor.putInt(it, m.getValue(it))
        }
        editor.apply()
        config = m
    }

    fun resetConfig() {
        saveTimerValues(defaultConfig)
    }

    fun saveConfig(config: Map<String, Int>) {
        saveTimerValues(config)
    }

    private fun resumeControls() {
        utils.toggleControls()
        button_on.isEnabled = false
    }

    @SuppressLint("ResourceType")
    private fun switchProg(v: ViewGroup) {
        val checked = v.progs_rb_group.checkedRadioButtonId
        if (checked > RGBPrograms.values().size - 1) {
            v.progs_rb_group.check(1)
        } else {
            v.progs_rb_group.check(checked + 1)
        }
    }
}
