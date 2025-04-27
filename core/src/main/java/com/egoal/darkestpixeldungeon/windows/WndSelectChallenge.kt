package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.CheckBox
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window

//todo: why not use an options window: i may decorate this later, 
open class WndSelectChallenge : Window() {
    private val challengeButtons: Map<Challenge, CheckBox>

    init {
        val title = PixelScene.renderMultiline(M.L(WndSelectChallenge::class.java, "title"), 8)
        title.maxWidth(WIDTH.toInt())
        add(title)

        var y = title.bottom() + 2f
        challengeButtons = Challenge.values().map {
            val btn = addChallengeButton(it, y)
            y = btn.bottom() + 2f
            Pair(it, btn)
        }.toMap()

        val btnConfirm = object : RedButton(M.L(WndSelectChallenge::class.java, "confirm")) {
            override fun onClick() {
                val checked = challengeButtons.filter { it.value.checked() }.map { it.key }.toList()
                activeChallenges(checked)
            }
        }.apply {
            setRect(MARGIN, y, (WIDTH - MARGIN * 2f), BTN_HEIGHT)
            enable(Badges.isUnlocked(Badges.Badge.VICTORY))
        }
        add(btnConfirm)
        y = btnConfirm.bottom()

        resize(WIDTH.toInt(), (y + GAP).toInt())
    }

    private fun addChallengeButton(challenge: Challenge, y: Float): CheckBox {
        val btn = object : CheckBox(challenge.title()) {
            override fun onClick() {
                super.onClick()
                refreshButtonState()
            }
        }.apply {
            setRect(MARGIN, y, (WIDTH - MARGIN * 3f - WIDTH_HELP_BUTTON), BTN_HEIGHT)
            if (Challenge.IsChallengePassed(challenge)) textColor(TITLE_COLOR)
        }
        add(btn)

        val btnInfo = object : RedButton("?") {
            override fun onClick() {
                GameScene.show(WndOptions(challenge.title(), challenge.desc()))
            }
        }.apply {
            setRect(WIDTH - MARGIN - WIDTH_HELP_BUTTON, y, WIDTH_HELP_BUTTON, BTN_HEIGHT)
        }
        add(btnInfo)

        return btn
    }

    private fun refreshButtonState() {
        val checked = challengeButtons.filter { it.value.checked() }.map { it.key }.toList()
        challengeButtons.forEach {
            it.value.enable(it.value.checked() || checked.all { c -> Challenge.IsCompatible(c, it.key) })
        }
    }

    protected open fun activeChallenges(challenges: List<Challenge>) {
        hide()
    }

    companion object {
        private const val WIDTH = 100f
        private const val BTN_HEIGHT = 16f
        private const val GAP = 2f
        private const val MARGIN = 2f
        private const val WIDTH_HELP_BUTTON = 15f
    }
}