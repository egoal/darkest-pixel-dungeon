package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.windows.InputDialog

open class InputButton(private val defText: String, size: Int) : RedButton(defText, size) {
    var inputText = defText
        private set

    open fun isValid(text: String) = true

    override fun onClick() {
        InputDialog.GetString(M.L(InputButton::class.java, "input"), defText) {
            if (isValid(it)) {
                inputText = it
                text(inputText)
            }
        }
    }
}