package home.tuxhome.moonlight

import android.content.Context
import android.content.SharedPreferences
import android.graphics.pdf.PdfDocument
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.loading_layout.view.*

class Utils(
    private val preferences: SharedPreferences,
    private val controls: ViewGroup,
    private val programsRG: ViewGroup,
    private var config: Map<String, Int>,
    private val defaultConfig: Map<String, Int>,
    private val defaultProgram: RGBPrograms
) {

//    fun getAllControls(parent: ViewGroup, allControls: ArrayList<Button>) {
//        for (i in 0 until parent.childCount) {
//            val child = parent.getChildAt(i)
//
//            if (child is Button) allControls.add(child)
//        }
//    }



    fun toggleControls() {
        for (index in 0 until controls.childCount)
            controls.getChildAt(index).isEnabled = !controls.getChildAt(index).isEnabled
    }

    fun disableControls() {
//        val parent = controls // radio buttons group
        for (index in 0 until controls.childCount)
            controls.getChildAt(index).isEnabled = false
    }

    fun toggleAllProgs() {
//        val parent = programsRG // radio buttons group
        for (index in 0 until programsRG.childCount)
            programsRG.getChildAt(index).isEnabled = !programsRG.getChildAt(index).isEnabled
    }


    fun saveProgram(name: String) {
        val editor = preferences.edit()
        editor.putString("program", name)
        editor.apply()
    }

    fun loadLastProgram(): String {
        return preferences.getString("program", defaultProgram.program_id)!!
    }

    fun initControls() {
        for (index in 1 until controls.childCount)
            controls.getChildAt(index).isEnabled = false
    }

    fun loadConfigValues(): MutableMap<String, Int> {
        return defaultConfig.mapValues { preferences.getInt(it.key, defaultConfig.getValue(it.key)) }
            .toMutableMap()
    }

}