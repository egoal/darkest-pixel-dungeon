package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.perks.ExtraPerkChoice
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.windows.WndGainNewPerk
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Image

class PerkSelectIndicator : Tag(0xff4c4c) {
    private lateinit var number: BitmapText
    private lateinit var icon: Image
    private var lastNumber = -100 //fixme

    init {
        setSize(24f, 16f)

        visible = false
    }

    override fun createChildren() {
        super.createChildren()

        bg.flipHorizontal(true)
        
        number = BitmapText(PixelScene.pixelFont)
        add(number)

        icon = Icons.PERK.get()
        add(icon)
    }

    override fun layout() {
        super.layout()

        icon.x = right() - 12
        icon.y = y + (height - icon.height) / 2

        placeNumber()
    }

    private fun placeNumber() {
        number.x = right() - 14f - number.width()
        number.y = y + (height - number.baseLine()) / 2f
        PixelScene.align(number)
    }

    override fun update() {
        val cnt = Dungeon.hero.reservedPerks
        visible = cnt > 0
        if (visible && lastNumber != cnt) {
            lastNumber = cnt
            number.text("$lastNumber")
            number.measure()
            placeNumber()

            flash()
        }

        super.update()
    }

    override fun onClick() {
        Dungeon.hero.reservedPerks--
        val cnt = if (Dungeon.hero.heroPerk.has(ExtraPerkChoice::class.java)) 5 else 3
        GameScene.show(WndGainNewPerk.CreateWithRandomPositives(cnt))
    }
}
