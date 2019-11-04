package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window

class WndMasterSubclass(way1: HeroSubClass, way2: HeroSubClass) : Window() {
    init {
        val rtm = PixelScene.renderMultiline(6)
        rtm.text(M.L(this, "message") + "\n\n" + way1.desc() + "\n\n" + way2.desc(), WIDTH.toInt())
        rtm.setPos(0f, 0f)
        add(rtm)

        val btnWay1 = object : RedButton(way1.title().toUpperCase()) {
            override fun onClick() {
                hide()
                HeroSubClass.Choose(Dungeon.hero!!, way1)
            }
        }
        btnWay1.setRect(0f, rtm.bottom() + GAP, (WIDTH - GAP) / 2f, BTN_HEIGHT)
        add(btnWay1)

        val btnWay2 = object : RedButton(way2.title().toUpperCase()) {
            override fun onClick() {
                hide()
                HeroSubClass.Choose(Dungeon.hero!!, way2)
            }
        }
        btnWay2.setRect(btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT)
        add(btnWay2)

        val btnCancel = object : RedButton(M.L(this, "cancel")) {
            override fun onClick() {
                hide()
                GameScene.show(WndMessage(M.L(WndMasterSubclass::class.java, "tip")))
            }
        }
        btnCancel.setRect(0f, btnWay2.bottom() + GAP, WIDTH, BTN_HEIGHT)
        add(btnCancel)

        resize(WIDTH.toInt(), btnCancel.bottom().toInt())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        GameScene.show(WndMessage(M.L(WndMasterSubclass::class.java, "tip")))
    }

    companion object {
        private const val WIDTH = 120f
        private const val BTN_HEIGHT = 18f
        private const val GAP = 2f

        fun Show(hero: Hero) {
            val pr = when (hero.heroClass) {
                HeroClass.WARRIOR -> Pair(HeroSubClass.GLADIATOR, HeroSubClass.BERSERKER)
                HeroClass.MAGE -> Pair(HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK)
                HeroClass.ROGUE -> Pair(HeroSubClass.FREERUNNER, HeroSubClass.ASSASSIN)
                HeroClass.HUNTRESS -> Pair(HeroSubClass.SNIPER, HeroSubClass.WARDEN)
                HeroClass.SORCERESS -> Pair(HeroSubClass.STARGAZER, HeroSubClass.WITCH)
            }

            GameScene.show(WndMasterSubclass(pr.first, pr.second))
        }
    }
}