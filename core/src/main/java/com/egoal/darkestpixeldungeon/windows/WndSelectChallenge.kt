package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window

//todo: why not use an options window: i may decorate this later, 
open class WndSelectChallenge : Window() {
    init {
        val title = PixelScene.renderMultiline(M.L(WndSelectChallenge::class.java, "title"), 8)
        title.maxWidth(WIDTH.toInt())
        add(title)

        var y = title.bottom() + 2f
        for (ch in Challenge.values()) y = addChallengeButton(ch, y + GAP)

        resize(WIDTH.toInt(), (y + GAP).toInt())
    }

    private fun addChallengeButton(challenge: Challenge, y: Float): Float {
        val unlocked = challenge == Challenge.LowPressure || Badges.isUnlocked(Badges.Badge.VICTORY) //todo:

        val btn = object : RedButton(challenge.title()) {
            override fun onClick() {
                GameScene.show(object : WndOptions(challenge.title(), challenge.desc(),
                        M.L(WndSelectChallenge::class.java, if (unlocked) "confirm" else "locked")) {
                    override fun onSelect(index: Int) {
                        if (unlocked && index == 0) onChallengeWouldActivate(challenge)
                    }
                })
            }
        }.apply {
            setRect(MARGIN, y, (WIDTH - MARGIN * 2f), BTN_HEIGHT)
            if (Challenge.IsChallengePassed(challenge)) textColor(TITLE_COLOR)
        }
        add(btn)

        return btn.bottom()
    }

    protected open fun onChallengeWouldActivate(challenge: Challenge) {
        hide()
    }

    companion object {
        private const val WIDTH = 100f
        private const val BTN_HEIGHT = 16f
        private const val GAP = 2f
        private const val MARGIN = 2f
    }
}