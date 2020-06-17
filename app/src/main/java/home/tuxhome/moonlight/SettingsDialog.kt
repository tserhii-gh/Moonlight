package home.tuxhome.moonlight

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.settings_layout.view.*
import kotlinx.android.synthetic.main.settings_layout.view.sync_start_value


class SettingsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val v = activity!!.layoutInflater.inflate(R.layout.settings_layout, null)
            val config = (activity!! as MainActivity).loadConfigValues()
            Log.e("Settings", config.mapValues { it.value }.toString())
            v.sync_start_value.setText(config.getValue("sync_startup").toString())
            v.sync_delay_value.setText(config.getValue("sync_delay").toString())
            v.switch_delay_value.setText(config.getValue("switch_delay").toString())
            v.save_delay_value.setText(config.getValue("save_delay").toString())
            builder.setView(v)
                .setPositiveButton(R.string.save) { _, id ->
                    config.put("sync_startup", v.sync_start_value.text.toString().toInt())
                    config.put("sync_delay", v.sync_delay_value.text.toString().toInt())
                    config.put("switch_delay", v.switch_delay_value.text.toString().toInt())
                    config.put("save_delay", v.save_delay_value.text.toString().toInt())
                    (activity!! as MainActivity).saveConfig(config)
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .setNeutralButton(R.string.reset) { _, _ -> (activity!! as MainActivity).resetConfig() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}