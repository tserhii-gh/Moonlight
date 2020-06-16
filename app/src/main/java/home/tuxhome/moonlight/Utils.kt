package home.tuxhome.moonlight

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.loading_layout.view.*

class Utils {
    //    private fun loadingDialog(ctx: Context): AlertDialog {
////        val load = ProgressDialog(this)
//        val builder = AlertDialog.Builder(ctx)
//        layoutInflater =
//        val v = layoutInflater.inflate(R.layout.loading_layout, null)
//        builder.setView(v)
////        v.loading_title.text = msg
////        builder.show()
//
//        return builder.create()
//    }
    fun getAllControls(parent: ViewGroup, allControls: ArrayList<Button>) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            if (child is Button) allControls.add(child)
        }
    }


}