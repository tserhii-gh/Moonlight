package home.tuxhome.moonlight

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.loading_layout.view.*
import kotlinx.android.synthetic.main.settings_layout.view.*

class LoadingDialog(private val msg: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val v = activity!!.layoutInflater.inflate(R.layout.loading_layout, null)
            builder.setView(v)
            v.loading_title.text = msg
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}