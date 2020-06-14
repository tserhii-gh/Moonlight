package home.tuxhome.moonlight

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.loading_layout.view.*
import kotlinx.android.synthetic.main.settings_layout.view.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    val SYNC_STARTUP_CONST = 3500
    val SYNC_DELAY_CONST = 12500
    val SWITCH_DELAY_CONST = 1500
    val SAVE_DELAY_CONST = 12500
    var SYNC_STARTUP = 0
    var SYNC_DELAY = 0
    var SWITCH_DELAY = 0
    var SAVE_DELAY = 0
    var APP_SETTINGS = "settings"
    var LIGHTS_UP = false
    lateinit var pref: SharedPreferences
    lateinit var handler: Handler
    private var allControls = ArrayList<Button>()

    //    private var allProgs = ArrayList<RadioButton>()
    val TAG = "Moonlight"
    val REALY_ON_URL = "http://192.168.2.101:10800/relay=on"
    val REALY_OFF_URL = "http://tuxnote:10800/relay=off"

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

        pref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
        reloadTimerValues()
        getAllControls(controls)

//        getAllProgs(progs_rb_group)


        handler = Handler()
        button_off.isEnabled = false
        button_next.isEnabled = false
        button_sync.isEnabled = false
        button_save.isEnabled = false
        button_on.setOnClickListener {
            enableAllProgs(progs_rb_group)
            button_on.isEnabled = false
            button_off.isEnabled = true
            button_next.isEnabled = true
            button_sync.isEnabled = true
            button_save.isEnabled = true
        }
        button_off.setOnClickListener {
            disableAllProgs(progs_rb_group)
            button_on.isEnabled = true
            button_off.isEnabled = false
            button_next.isEnabled = false
            button_sync.isEnabled = false
            button_save.isEnabled = false
        }

        button_save.setOnClickListener {
            allControls.forEach {
                it.isEnabled = false
            }
            LIGHTS_UP = false
//            Log.e(TAG, allProgs.size.toString())
            disableAllProgs(progs_rb_group)
            val dlg = loadingDialog("Saving selected PROGRAM")
            dlg.show()
            handler.postDelayed({
                Log.e(TAG, "Thread")
                dlg.dismiss()
                LIGHTS_UP = true
                button_on.isEnabled = false
                button_off.isEnabled = true
                button_next.isEnabled = true
                button_sync.isEnabled = true
                button_save.isEnabled = true
                enableAllProgs(progs_rb_group)
//                allButtons.forEach {
//                    it.isEnabled = true
//                }
            }, SAVE_DELAY.toLong())


        }

        button_next.setOnClickListener {
            allControls.forEach {
                it.isEnabled = false
            }
            LIGHTS_UP = false
            handler.postDelayed({
                Log.e(TAG, "Thread")
                switchProg(progs_rb_group)
                LIGHTS_UP = true
                button_on.isEnabled = false
                button_off.isEnabled = true
                button_next.isEnabled = true
                button_sync.isEnabled = true
                button_save.isEnabled = true
                enableAllProgs(progs_rb_group)
//                allButtons.forEach {
//                    it.isEnabled = true
//                }
            }, SWITCH_DELAY.toLong())
        }

        if (progs_rb_group != null) {
            PROGS.forEach {

//                if (it == "neutral_white"){
//                    progs_rb_group.addView(
//                        RadioButton(this).apply {
//                            text = PROG_INFO[PROGS.indexOf(it)]
////                        isEnabled = false
////                            isClickable=false
////                            isChecked = true
//                            id = it
//                        }
//                    )}
//                else {
                progs_rb_group.addView(
                    RadioButton(this).apply {
                        text = PROG_INFO[PROGS.indexOf(it)]
//                        isEnabled = false
//                        isClickable=false
//                            id = it
                        tag = it
                    }
                )
//                }
            }
//            this.currentFocus
            progs_rb_group.addView(TextView(this).apply {
            })
            progs_rb_group.setOnCheckedChangeListener { group, checkedId ->
                Toast.makeText(applicationContext, checkedId.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        progs_rb_group.check(11)
        disableAllProgs(progs_rb_group)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.settigs_action) {
//            Toast.makeText(this, "Item One Clicked", Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(this)
            val v = layoutInflater.inflate(R.layout.settings_layout, null)
            builder.setView(v)
            builder.setPositiveButton(R.string.save) { dialog, which ->
                saveTimerValues(
                    v.sync_start_value.text.toString(),
                    v.sync_delay_value.text.toString(),
                    v.switch_delay_value.text.toString(),
                    v.save_delay_value.text.toString()
                )
                reloadTimerValues()
                Toast.makeText(
                    applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT
                ).show()
            }

            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.no, Toast.LENGTH_SHORT
                ).show()
            }

            builder.setNeutralButton("Reset") { dialog, which ->
                resetTimerValues()
                Toast.makeText(
                    applicationContext,
                    "Config saved with default values", Toast.LENGTH_SHORT
                ).show()
            }
            reloadTimerValues()
            initTimerValues(v)
            builder.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun reloadTimerValues() {
//        if (pref.contains(APP_SETTINGS)){
        SYNC_STARTUP = pref.getInt("sync_startup", SYNC_STARTUP_CONST)
        SYNC_DELAY = pref.getInt("sync_delay", SYNC_DELAY_CONST)
        SWITCH_DELAY = pref.getInt("switch_delay", SWITCH_DELAY_CONST)
        SAVE_DELAY = pref.getInt("save_delay", SAVE_DELAY_CONST)
    }

    private fun initTimerValues(v: View) {
        v.sync_start_value.setText(SYNC_STARTUP.toString())
        v.sync_delay_value.setText(SYNC_DELAY.toString())
        v.switch_delay_value.setText(SWITCH_DELAY.toString())
        v.save_delay_value.setText(SAVE_DELAY.toString())
        // = SYNC_STARTUP
    }

    private fun saveTimerValues(st: String, sd: String, sw: String, sa: String) {
        val editor = pref.edit()
        editor.putInt("sync_startup", st.toInt())
        editor.putInt("sync_delay", sd.toInt())
        editor.putInt("switch_delay", sw.toInt())
        editor.putInt("save_delay", sa.toInt())
        editor.apply()
    }

    private fun resetTimerValues() {
        val editor = pref.edit()
        editor.putInt("sync_startup", SYNC_STARTUP_CONST)
        editor.putInt("sync_delay", SYNC_DELAY_CONST)
        editor.putInt("switch_delay", SWITCH_DELAY_CONST)
        editor.putInt("save_delay", SAVE_DELAY_CONST)
        editor.apply()
        reloadTimerValues()
    }

    private fun getAllControls(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            if (child is Button) allControls.add(child)
            else if (child is ViewGroup) getAllControls(child)
        }
    }

    private fun disableAllProgs(parent: ViewGroup) {
        for (index in 0..parent.childCount - 1)
            parent.getChildAt(index).isEnabled = false
    }

    private fun enableAllProgs(parent: ViewGroup) {
        for (index in 0..parent.childCount - 1)
            parent.getChildAt(index).isEnabled = true
    }

    private fun remoteRelayON() {
        Thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(REALY_ON_URL)
                .build()
            val resp = client.newCall(request).execute()
            Log.e(TAG, resp.code.toString())
        }.start()
    }

    private fun remoteRelayOFF() {
        Thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(REALY_OFF_URL)
                .build()
            val resp = client.newCall(request).execute()
            Log.e(TAG, resp.code.toString())
        }.start()
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
}