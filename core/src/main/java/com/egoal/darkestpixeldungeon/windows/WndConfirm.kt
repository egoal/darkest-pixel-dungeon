package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.Image

class WndConfirm(icon: Image, title: String, message: String, private val onConfirmed: () -> Unit)
    : WndOptions(icon, title, message, M.L(WndConfirm::class.java, "yes"), M.L(WndConfirm::class.java, "no")) {
    override fun onSelect(index: Int) {
        if (index == 0) onConfirmed()
    }
}