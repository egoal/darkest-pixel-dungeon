package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.windows.WndMessage
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.BitmapText
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.Image
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.ui.Button

open class PerkSlot(protected val perk: Perk) : Button() {
    private lateinit var icon: Image
    private lateinit var bottomRight: BitmapText

    init {
        setPerk()
    }

    private fun setPerk() {
        val index = perk.image()
        icon.frame(film.get(index))

        if (perk.level > 1 || perk.upgradable()) {
            bottomRight.text("${perk.level}")
            bottomRight.hardlight(0xf1ca3e)
            bottomRight.measure()
        } else bottomRight.visible = false
    }

    override fun createChildren() {
        super.createChildren()

        icon = Image(icons)
        add(icon)

        bottomRight = BitmapText(PixelScene.pixelFont)
        add(bottomRight)
    }

    override fun layout() {
        super.layout()

        icon.x = x + (width - icon.width) / 2
        icon.y = y + (height - icon.height) / 2

        bottomRight.x = x + (width - bottomRight.width())
        bottomRight.y = y + (height - bottomRight.height())
    }

    override fun onClick() {
        GameScene.show(WndMessage(perk.description()))
    }

    companion object {
        private val icons = TextureCache.get(Assets.PERKS)
        private val film = TextureFilm(icons, 16, 16)
    }

}