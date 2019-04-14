package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.Image

class ResumeIndicator : Tag(0xCDD5C0) {
    init {
        setSize(24f, 24f)

        visible = false
    }

    private lateinit var icon: Image

    override fun createChildren() {
        super.createChildren()

        icon = Icons.get(Icons.RESUME)
        add(icon)
    }

    override fun layout() {
        super.layout()

        icon.x = x + 1f + (width - icon.width) / 2f
        icon.y = y + (height - icon.height) / 2f
        PixelScene.align(icon)
    }

    override fun onClick() {
        Dungeon.hero.resume()
    }

    override fun update() {
        if (!Dungeon.hero.isAlive)
            visible = false
        else if (visible != (Dungeon.hero.lastAction != null)) {
            visible = Dungeon.hero.lastAction != null

            if (visible) flash()
        }

        super.update()
    }

}