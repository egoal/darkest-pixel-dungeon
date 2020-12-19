package com.egoal.darkestpixeldungeon.windows

import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.EditText
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.Game

//todo: this is not a good design, just for very rare & lite use.
object InputDialog : DialogInterface.OnClickListener {
    private val strOK = M.L(InputDialog::class.java, "ok")
    private val strCANCEL = M.L(InputDialog::class.java, "cancel")

    private lateinit var action: (String) -> Unit
    private lateinit var editText: EditText
    private var defStr: String = ""

    override fun onClick(dialog: DialogInterface, which: Int) {
        val input = if (which == DialogInterface.BUTTON_POSITIVE) editText.text else defStr
        action(input.toString())
    }

    private fun showDialog(title: String, defval: String) {
        val alert = AlertDialog.Builder(Game.instance)
        alert.setTitle(title)
        alert.setPositiveButton(strOK, this)
        alert.setNegativeButton(strCANCEL, this)
        alert.setCancelable(false)

        editText = EditText(Game.instance)
        editText.setSingleLine()
        editText.setText(defval)

        alert.setView(editText)

        alert.show()
    }

    //
    fun GetString(title: String, defval: String, onInputed: (String) -> Unit) {
        action = onInputed
        defStr = defval

        Game.instance.runOnUiThread {
            showDialog(title, defval)
        }
    }
}